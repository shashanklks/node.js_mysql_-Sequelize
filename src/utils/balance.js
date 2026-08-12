// utils/balance.js
const { fn, col, literal } = require('sequelize');
const Entry = require('../model/Entry');

// Ledger convention, matching Khatabook:
//   balance = SUM(GAVE) - SUM(GOT)
//   balance > 0  -> "You will get"  (shown in green)
//   balance < 0  -> "You will give" (shown in red)
const round2 = (value) => Math.round((Number(value) + Number.EPSILON) * 100) / 100;

const balanceOf = (gave, got) => round2(Number(gave || 0) - Number(got || 0));

// Returns { [partyId]: { gave, got, balance, lastEntryDate } }
async function balancesByParty(userId, partyIds = null) {
  const where = { userId };
  if (partyIds && partyIds.length) where.partyId = partyIds;

  const rows = await Entry.findAll({
    attributes: [
      'partyId',
      [fn('SUM', literal("CASE WHEN `type` = 'GAVE' THEN amount ELSE 0 END")), 'gave'],
      [fn('SUM', literal("CASE WHEN `type` = 'GOT' THEN amount ELSE 0 END")), 'got'],
      [fn('MAX', col('entryDate')), 'lastEntryDate'],
    ],
    where,
    group: ['partyId'],
    raw: true,
  });

  const map = {};
  rows.forEach((row) => {
    const gave = round2(row.gave || 0);
    const got = round2(row.got || 0);
    map[row.partyId] = {
      gave,
      got,
      balance: balanceOf(gave, got),
      lastEntryDate: row.lastEntryDate || null,
    };
  });
  return map;
}

/**
 * Walks a chronological list of entries and returns the balance after each one,
 * plus the totals for the whole list. Pure, so the ledger view and the tests
 * agree on what a running balance means.
 */
function runningBalances(entries) {
  let gave = 0;
  let got = 0;
  const balances = entries.map((entry) => {
    if (entry.type === 'GAVE') gave = round2(gave + Number(entry.amount));
    else got = round2(got + Number(entry.amount));
    return balanceOf(gave, got);
  });
  return { balances, gave, got, balance: balanceOf(gave, got) };
}

module.exports = { round2, balanceOf, balancesByParty, runningBalances };
