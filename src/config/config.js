// config/config.js
const { Sequelize } = require('sequelize');
require('dotenv').config();

const sequelize = new Sequelize(
  process.env.DB_NAME || 'khatabook',
  process.env.DB_USER || 'root',
  process.env.DB_PASSWORD || 'root@123',
  {
    host: process.env.DB_HOST || 'localhost',
    port: Number(process.env.DB_PORT || 3306),
    dialect: 'mysql',
    logging: process.env.DB_LOGGING === 'true' ? console.log : false,
    define: {
      underscored: false,
    },
  }
);

// Authenticate and sync every model that has been registered so far.
// Called explicitly from src/app.js so that importing this module never
// triggers a connection attempt as a side effect.
async function syncDatabase() {
  await sequelize.authenticate();
  console.log('Connection to the database has been established successfully.');

  await sequelize.sync({ alter: process.env.DB_ALTER === 'true' });
  console.log('All models were synchronized successfully.');
}

module.exports = sequelize;
module.exports.sequelize = sequelize;
module.exports.syncDatabase = syncDatabase;
