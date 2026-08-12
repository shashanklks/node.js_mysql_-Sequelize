const test = require('node:test');
const assert = require('node:assert');
const { round2, balanceOf, runningBalances } = require('../src/utils/balance');

test('balanceOf is positive when the party still owes you', () => {
  assert.strictEqual(balanceOf(500, 200), 300);
});

test('balanceOf is negative when you owe the party', () => {
  assert.strictEqual(balanceOf(200, 500), -300);
});

test('balanceOf is zero once the account is settled', () => {
  assert.strictEqual(balanceOf(750.5, 750.5), 0);
});

test('round2 keeps paise from drifting', () => {
  assert.strictEqual(round2(0.1 + 0.2), 0.3);
  assert.strictEqual(round2(1234.567), 1234.57);
});

test('runningBalances walks the ledger the way the app shows it', () => {
  const entries = [
    { type: 'GAVE', amount: 1000 }, // sold on credit
    { type: 'GOT', amount: 400 }, // part payment
    { type: 'GAVE', amount: 250.75 },
    { type: 'GOT', amount: 850.75 }, // settled
  ];

  const result = runningBalances(entries);

  assert.deepStrictEqual(result.balances, [1000, 600, 850.75, 0]);
  assert.strictEqual(result.gave, 1250.75);
  assert.strictEqual(result.got, 1250.75);
  assert.strictEqual(result.balance, 0);
});

test('runningBalances handles a supplier book going negative', () => {
  const result = runningBalances([
    { type: 'GOT', amount: 5000 }, // stock received on credit
    { type: 'GAVE', amount: 2000 }, // part payment made
  ]);

  assert.deepStrictEqual(result.balances, [-5000, -3000]);
  assert.strictEqual(result.balance, -3000);
});

test('runningBalances copes with the decimal strings MySQL returns', () => {
  const result = runningBalances([
    { type: 'GAVE', amount: '10.10' },
    { type: 'GAVE', amount: '20.20' },
    { type: 'GOT', amount: '0.30' },
  ]);

  assert.deepStrictEqual(result.balances, [10.1, 30.3, 30]);
});

test('an empty ledger is a settled ledger', () => {
  const result = runningBalances([]);
  assert.deepStrictEqual(result.balances, []);
  assert.strictEqual(result.balance, 0);
});
