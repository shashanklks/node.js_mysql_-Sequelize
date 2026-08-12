// controllers/reportController.js
const Joi = require('joi');
const { Op } = require('sequelize');
const { Entry, Party } = require('../model');
const { balancesByParty, round2, runningBalances } = require('../utils/balance');
const { ok, fail, asyncHandler } = require('../utils/respond');

// Home header + report header: the two totals of the whole book, split by tab.
exports.summary = asyncHandler(async (req, res) => {
  const parties = await Party.findAll({ where: { userId: req.user.id } });
  const stats = await balancesByParty(req.user.id);

  const blank = () => ({ youWillGet: 0, youWillGive: 0, count: 0 });
  const totals = { CUSTOMER: blank(), SUPPLIER: blank() };

  parties.forEach((party) => {
    const bucket = totals[party.type];
    bucket.count += 1;
    const balance = stats[party.id] ? stats[party.id].balance : 0;
    if (balance > 0) bucket.youWillGet = round2(bucket.youWillGet + balance);
    else if (balance < 0) bucket.youWillGive = round2(bucket.youWillGive - balance);
  });

  return ok(res, {
    customers: totals.CUSTOMER,
    suppliers: totals.SUPPLIER,
    overall: {
      youWillGet: round2(totals.CUSTOMER.youWillGet + totals.SUPPLIER.youWillGet),
      youWillGive: round2(totals.CUSTOMER.youWillGive + totals.SUPPLIER.youWillGive),
      count: parties.length,
    },
  });
});

// Cashbook: every entry in a date range, newest first, with day-wise totals.
exports.transactions = asyncHandler(async (req, res) => {
  const { error, value } = Joi.object({
    from: Joi.string().pattern(/^\d{4}-\d{2}-\d{2}$/),
    to: Joi.string().pattern(/^\d{4}-\d{2}-\d{2}$/),
    type: Joi.string().valid('GAVE', 'GOT'),
    partyType: Joi.string().valid('CUSTOMER', 'SUPPLIER'),
    limit: Joi.number().integer().min(1).max(500).default(200),
  }).validate(req.query);
  if (error) return fail(res, error.details[0].message, 400);

  const where = { userId: req.user.id };
  if (value.type) where.type = value.type;
  if (value.from && value.to) where.entryDate = { [Op.between]: [value.from, value.to] };
  else if (value.from) where.entryDate = { [Op.gte]: value.from };
  else if (value.to) where.entryDate = { [Op.lte]: value.to };

  const partyWhere = { userId: req.user.id };
  if (value.partyType) partyWhere.type = value.partyType;

  const entries = await Entry.findAll({
    where,
    include: [{ model: Party, attributes: ['id', 'name', 'type'], where: partyWhere, required: true }],
    order: [
      ['entryDate', 'DESC'],
      ['id', 'DESC'],
    ],
    limit: value.limit,
  });

  const { gave, got, balance } = runningBalances(entries);
  const items = entries.map((entry) => {
    return {
      id: entry.id,
      partyId: entry.partyId,
      partyName: entry.Party ? entry.Party.name : null,
      partyType: entry.Party ? entry.Party.type : null,
      amount: Number(entry.amount),
      type: entry.type,
      note: entry.note || null,
      entryDate: entry.entryDate,
      createdAt: entry.createdAt,
    };
  });

  return ok(res, {
    entries: items,
    summary: { gave, got, net: balance, count: items.length },
    range: { from: value.from || null, to: value.to || null },
  });
});
