// controllers/partyController.js
const Joi = require('joi');
const { Op } = require('sequelize');
const { Party, Entry } = require('../model');
const { balancesByParty, round2 } = require('../utils/balance');
const { ok, fail, asyncHandler } = require('../utils/respond');

const partySchema = Joi.object({
  name: Joi.string().trim().min(1).max(100).required(),
  phone: Joi.string()
    .pattern(/^[0-9]{10,15}$/)
    .allow('', null)
    .messages({ 'string.pattern.base': 'Enter a valid mobile number' }),
  address: Joi.string().trim().max(255).allow('', null),
  type: Joi.string().valid('CUSTOMER', 'SUPPLIER').default('CUSTOMER'),
});

// An update only touches the fields it names, so no defaults here.
const updatePartySchema = Joi.object({
  name: Joi.string().trim().min(1).max(100),
  phone: Joi.string()
    .pattern(/^[0-9]{10,15}$/)
    .allow('', null)
    .messages({ 'string.pattern.base': 'Enter a valid mobile number' }),
  address: Joi.string().trim().max(255).allow('', null),
  type: Joi.string().valid('CUSTOMER', 'SUPPLIER'),
}).min(1);

const shape = (party, stats) => ({
  id: party.id,
  name: party.name,
  phone: party.phone || null,
  address: party.address || null,
  type: party.type,
  gave: stats ? stats.gave : 0,
  got: stats ? stats.got : 0,
  balance: stats ? stats.balance : 0,
  lastEntryDate: stats ? stats.lastEntryDate : null,
  createdAt: party.createdAt,
});

const findParty = (userId, id) => Party.findOne({ where: { id, userId } });

exports.listParties = asyncHandler(async (req, res) => {
  const { error, value } = Joi.object({
    type: Joi.string().valid('CUSTOMER', 'SUPPLIER'),
    search: Joi.string().trim().allow(''),
    sort: Joi.string().valid('recent', 'name', 'highest', 'lowest').default('recent'),
  }).validate(req.query);
  if (error) return fail(res, error.details[0].message, 400);

  const where = { userId: req.user.id };
  if (value.type) where.type = value.type;
  if (value.search) {
    where[Op.or] = [
      { name: { [Op.like]: `%${value.search}%` } },
      { phone: { [Op.like]: `%${value.search}%` } },
    ];
  }

  const parties = await Party.findAll({ where, order: [['name', 'ASC']] });
  const stats = await balancesByParty(
    req.user.id,
    parties.map((p) => p.id)
  );

  const items = parties.map((party) => shape(party, stats[party.id]));

  const comparators = {
    name: (a, b) => a.name.localeCompare(b.name),
    highest: (a, b) => b.balance - a.balance,
    lowest: (a, b) => a.balance - b.balance,
    recent: (a, b) => {
      const aDate = a.lastEntryDate || '';
      const bDate = b.lastEntryDate || '';
      if (aDate !== bDate) return bDate.localeCompare(aDate);
      return a.name.localeCompare(b.name);
    },
  };
  items.sort(comparators[value.sort]);

  // Home screen header: what the whole book owes in each direction.
  const summary = items.reduce(
    (acc, item) => {
      if (item.balance > 0) acc.youWillGet = round2(acc.youWillGet + item.balance);
      else if (item.balance < 0) acc.youWillGive = round2(acc.youWillGive - item.balance);
      return acc;
    },
    { youWillGet: 0, youWillGive: 0 }
  );

  return ok(res, { parties: items, summary, count: items.length });
});

exports.createParty = asyncHandler(async (req, res) => {
  const { error, value } = partySchema.validate(req.body);
  if (error) return fail(res, error.details[0].message, 400);

  const duplicate = await Party.findOne({
    where: { userId: req.user.id, type: value.type, name: value.name },
  });
  if (duplicate) return fail(res, `${value.name} already exists in this book`, 409);

  const party = await Party.create({
    userId: req.user.id,
    name: value.name,
    phone: value.phone || null,
    address: value.address || null,
    type: value.type,
  });

  return ok(res, { party: shape(party, null) }, 'Saved successfully', 201);
});

exports.getParty = asyncHandler(async (req, res) => {
  const party = await findParty(req.user.id, req.params.id);
  if (!party) return fail(res, 'Not found', 404);

  const stats = await balancesByParty(req.user.id, [party.id]);
  return ok(res, { party: shape(party, stats[party.id]) });
});

exports.updateParty = asyncHandler(async (req, res) => {
  const { error, value } = updatePartySchema.validate(req.body);
  if (error) return fail(res, error.details[0].message, 400);

  const party = await findParty(req.user.id, req.params.id);
  if (!party) return fail(res, 'Not found', 404);

  await party.update(value);
  const stats = await balancesByParty(req.user.id, [party.id]);
  return ok(res, { party: shape(party, stats[party.id]) }, 'Updated successfully');
});

exports.deleteParty = asyncHandler(async (req, res) => {
  const party = await findParty(req.user.id, req.params.id);
  if (!party) return fail(res, 'Not found', 404);

  await Entry.destroy({ where: { partyId: party.id, userId: req.user.id } });
  await party.destroy();
  return ok(res, {}, 'Deleted successfully');
});

exports.findParty = findParty;
exports.shapeParty = shape;
