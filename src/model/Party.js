// models/Party.js
const { DataTypes } = require('sequelize');
const sequelize = require('../config/config');

// A "party" is either a customer (someone who owes you) or a supplier
// (someone you owe). Khatabook keeps them in two tabs of the same book.
const Party = sequelize.define(
  'Party',
  {
    userId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    name: {
      type: DataTypes.STRING,
      allowNull: false,
    },
    phone: {
      type: DataTypes.STRING(20),
      allowNull: true,
    },
    address: {
      type: DataTypes.STRING,
      allowNull: true,
    },
    type: {
      type: DataTypes.ENUM('CUSTOMER', 'SUPPLIER'),
      allowNull: false,
      defaultValue: 'CUSTOMER',
    },
  },
  {
    indexes: [{ fields: ['userId', 'type'] }],
  }
);

module.exports = Party;
