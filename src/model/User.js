// models/User.js
const { DataTypes } = require('sequelize');
const sequelize = require('../config/config');

const User = sequelize.define('User', {
  firstName: {
    type: DataTypes.STRING,
    allowNull: true,
  },
  lastName: {
    type: DataTypes.STRING,
    allowNull: true,
  },
  username: {
    type: DataTypes.STRING,
    allowNull: true,
    unique: true,
  },
  email: {
    type: DataTypes.STRING,
    allowNull: true,
    unique: true,
  },
  password: {
    type: DataTypes.STRING,
    allowNull: true,
  },
  // Khatabook style login is phone + OTP, so the phone number is the
  // real identity of an account. Email/password stay supported for the
  // original REST clients.
  phone: {
    type: DataTypes.STRING(20),
    allowNull: true,
    unique: true,
  },
  name: {
    type: DataTypes.STRING,
    allowNull: true,
  },
  businessName: {
    type: DataTypes.STRING,
    allowNull: true,
  },
  language: {
    type: DataTypes.STRING(8),
    allowNull: false,
    defaultValue: 'en',
  },
  profileCompleted: {
    type: DataTypes.BOOLEAN,
    allowNull: false,
    defaultValue: false,
  },
});

module.exports = User;
