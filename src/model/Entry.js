// models/Entry.js
const { DataTypes } = require('sequelize');
const sequelize = require('../config/config');

// One line of the ledger. GAVE = you gave money/goods (party owes you),
// GOT = you received money from the party.
const Entry = sequelize.define(
  'Entry',
  {
    userId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    partyId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    amount: {
      type: DataTypes.DECIMAL(12, 2),
      allowNull: false,
      get() {
        const raw = this.getDataValue('amount');
        return raw === null ? null : Number(raw);
      },
    },
    type: {
      type: DataTypes.ENUM('GAVE', 'GOT'),
      allowNull: false,
    },
    note: {
      type: DataTypes.STRING(255),
      allowNull: true,
    },
    entryDate: {
      type: DataTypes.DATEONLY,
      allowNull: false,
    },
  },
  {
    indexes: [{ fields: ['partyId', 'entryDate'] }, { fields: ['userId', 'entryDate'] }],
  }
);

module.exports = Entry;
