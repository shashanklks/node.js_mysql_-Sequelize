// models/index.js
const sequelize = require('../config/config');
const User = require('./User');
const Party = require('./Party');
const Entry = require('./Entry');
const Otp = require('./Otp');

User.hasMany(Party, { foreignKey: 'userId', onDelete: 'CASCADE' });
Party.belongsTo(User, { foreignKey: 'userId' });

Party.hasMany(Entry, { foreignKey: 'partyId', onDelete: 'CASCADE' });
Entry.belongsTo(Party, { foreignKey: 'partyId' });

User.hasMany(Entry, { foreignKey: 'userId', onDelete: 'CASCADE' });
Entry.belongsTo(User, { foreignKey: 'userId' });

module.exports = { sequelize, User, Party, Entry, Otp };
