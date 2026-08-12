// routes/apiRoutes.js — everything the Android client talks to.
const express = require('express');
const authController = require('../controllers/authController');
const partyController = require('../controllers/partyController');
const entryController = require('../controllers/entryController');
const reportController = require('../controllers/reportController');
const { authenticateToken } = require('../utils/authMiddleware');

const router = express.Router();

router.post('/auth/send-otp', authController.sendOtp);
router.post('/auth/verify-otp', authController.verifyOtp);

router.use(authenticateToken);

router.get('/profile', authController.getProfile);
router.put('/profile', authController.updateProfile);

router.get('/parties', partyController.listParties);
router.post('/parties', partyController.createParty);
router.get('/parties/:id', partyController.getParty);
router.put('/parties/:id', partyController.updateParty);
router.delete('/parties/:id', partyController.deleteParty);

router.get('/parties/:partyId/entries', entryController.listEntries);
router.post('/parties/:partyId/entries', entryController.createEntry);
router.put('/entries/:id', entryController.updateEntry);
router.delete('/entries/:id', entryController.deleteEntry);

router.get('/reports/summary', reportController.summary);
router.get('/reports/transactions', reportController.transactions);

module.exports = router;
