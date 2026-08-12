// controllers/entryController.js
const Joi = require('joi');
const { Entry, Party } = require('../model');
const { round2, runningBalances } = require('../utils/balance');
const { ok, fail, asyncHandler } = require('../utils/respond');

const today = () => new Date().toISOString().slice(0, 10);

const entrySchema = Joi.object({
  amount: Joi.number().positive().precision(2).max(99999999).required(),
  type: Joi.string().valid('GAVE', 'GOT').required(),
  note: Joi.string().trim().max(255).allow('', null),
  entryDate: Joi.string()
    .pattern(/^\d{4}-\d{2}-\d{2}$/)
    .default(today)
    .messages({ 'string.pattern.base': 'Date must look like YYYY-MM-DD' }),
});

const shape = (entry, runningBalance) => ({
  id: entry.id,
  partyId: entry.partyId,
  amount: Number(entry.amount),
  type: entry.type,
  note: entry.note || null,
  entryDate: entry.entryDate,
  createdAt: entry.createdAt,
  ...(runningBalance === undefined ? {} : { runningBalance }),
});

// Oldest first so the running balance accumulates the way a paper ledger does.
const chronological = [
  ['entryDate', 'ASC'],
  ['id', 'ASC'],
];

exports.listEntries = asyncHandler(async (req, res) => {
  const party = await Party.findOne({ where: { id: req.params.partyId, userId: req.user.id } });
  if (!party) return fail(res, 'Not found', 404);

  const entries = await Entry.findAll({
    where: { partyId: party.id, userId: req.user.id },
    order: chronological,
  });

  const { balances, gave, got, balance } = runningBalances(entries);
  const items = entries.map((entry, index) => shape(entry, balances[index]));

  return ok(res, {
    party: {
      id: party.id,
      name: party.name,
      phone: party.phone || null,
      type: party.type,
    },
    entries: items,
    summary: { gave, got, balance },
  });
});

exports.createEntry = asyncHandler(async (req, res) => {
  const { error, value } = entrySchema.validate(req.body);
  if (error) return fail(res, error.details[0].message, 400);

  const party = await Party.findOne({ where: { id: req.params.partyId, userId: req.user.id } });
  if (!party) return fail(res, 'Not found', 404);

  const entry = await Entry.create({
    userId: req.user.id,
    partyId: party.id,
    amount: round2(value.amount),
    type: value.type,
    note: value.note || null,
    entryDate: value.entryDate,
  });

  return ok(res, { entry: shape(entry) }, 'Entry saved', 201);
});

exports.updateEntry = asyncHandler(async (req, res) => {
  const { error, value } = Joi.object({
    amount: Joi.number().positive().precision(2).max(99999999),
    type: Joi.string().valid('GAVE', 'GOT'),
    note: Joi.string().trim().max(255).allow('', null),
    entryDate: Joi.string().pattern(/^\d{4}-\d{2}-\d{2}$/),
  })
    .min(1)
    .validate(req.body);
  if (error) return fail(res, error.details[0].message, 400);

  const entry = await Entry.findOne({ where: { id: req.params.id, userId: req.user.id } });
  if (!entry) return fail(res, 'Not found', 404);

  await entry.update(value);
  return ok(res, { entry: shape(entry) }, 'Entry updated');
});

exports.deleteEntry = asyncHandler(async (req, res) => {
  const entry = await Entry.findOne({ where: { id: req.params.id, userId: req.user.id } });
  if (!entry) return fail(res, 'Not found', 404);

  await entry.destroy();
  return ok(res, {}, 'Entry deleted');
});
