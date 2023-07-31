// config/config.js
const { Sequelize } = require('sequelize');

const sequelize = new Sequelize('data', 'root', 'root@123', {
  host: 'localhost',
  dialect: 'mysql',
});

// Test the database connection
// Test the database connection and synchronize models
async function syncDatabase() {
    try {
      await sequelize.authenticate();
      console.log('Connection to the database has been established successfully.');
  
      // Synchronize the models with the database tables
      await sequelize.sync();
  
      console.log('All models were synchronized successfully.');
    } catch (error) {
      console.error('Unable to connect to the database:', error);
    }
  }
  
  // Call the function to test the connection and synchronize the models
  syncDatabase();
module.exports = sequelize;