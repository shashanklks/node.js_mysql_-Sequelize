// src/app.js
require('dotenv').config();

const express = require('express');
const authRoutes = require('./routes/authRoutes');
const apiRoutes = require('./routes/apiRoutes');
const { syncDatabase } = require('./config/config');

const app = express();

app.use(express.json());

// The Android emulator and any local web client talk to this server directly.
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', 'Origin, Content-Type, Accept, Authorization');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH, DELETE, OPTIONS');
  if (req.method === 'OPTIONS') return res.sendStatus(204);
  return next();
});

app.get('/health', (req, res) =>
  res.json({ statusCode: 200, success: true, message: 'ok', data: { uptime: process.uptime() } })
);

app.use('/api', apiRoutes);
app.use('/', authRoutes);

app.use((req, res) =>
  res.status(404).json({ statusCode: 404, success: false, message: 'Route not found' })
);

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ statusCode: 500, success: false, message: 'Something went wrong' });
});

process.on('uncaughtException', (error) => {
  console.error('Uncaught Exception:', error);
  process.exit(1);
});

process.on('unhandledRejection', (reason) => {
  console.error('Unhandled Promise Rejection:', reason);
});

const PORT = process.env.PORT || 3000;

if (require.main === module) {
  syncDatabase()
    .then(() => {
      app.listen(PORT, () => console.log(`Server is running on port ${PORT}`));
    })
    .catch((error) => {
      console.error('Unable to connect to the database:', error);
      process.exit(1);
    });
}

module.exports = app;
