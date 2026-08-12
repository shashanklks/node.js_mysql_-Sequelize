// models/Otp.js
const { DataTypes } = require('sequelize');
const sequelize = require('../config/config');

const Otp = sequelize.define(
  'Otp',
  {
    phone: {
      type: DataTypes.STRING(20),
      allowNull: false,
    },
    code: {
      type: DataTypes.STRING(10),
      allowNull: false,
    },
    expiresAt: {
      type: DataTypes.DATE,
      allowNull: false,
    },
    consumed: {
      type: DataTypes.BOOLEAN,
      allowNull: false,
      defaultValue: false,
    },
    attempts: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 0,
    },
  },
  {
    indexes: [{ fields: ['phone'] }],
  }
);

module.exports = Otp;
