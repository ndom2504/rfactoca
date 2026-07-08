const stripe = require('stripe');
const pool = require('../../../lib/db');

module.exports = async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  try {
    const { parcelId, amountCad, currency = 'CAD', userId } = req.body;

    // Validate required fields
    if (!parcelId || !amountCad || !userId) {
      return res.status(400).json({ error: 'Missing required fields: parcelId, amountCad, userId' });
    }

    // Verify parcel exists
    const parcel = await pool.query(
      'SELECT * FROM parcels WHERE id = $1',
      [parcelId]
    );

    if (parcel.rows.length === 0) {
      return res.status(404).json({ error: 'Parcel not found' });
    }

    // Check if user already has a Stripe customer ID
    const user = await pool.query(
      'SELECT stripe_customer_id FROM users WHERE id = $1',
      [userId]
    );

    let customerId;
    if (user.rows.length > 0 && user.rows[0].stripe_customer_id) {
      // Use existing customer
      customerId = user.rows[0].stripe_customer_id;
    } else {
      // Create new customer
      const stripeInstance = stripe(process.env.STRIPE_SECRET_KEY);
      const customer = await stripeInstance.customers.create({
        email: user.rows[0]?.email,
        metadata: { userId: userId.toString() }
      });
      customerId = customer.id;

      // Save customer ID to database
      await pool.query(
        'UPDATE users SET stripe_customer_id = $1 WHERE id = $2',
        [customerId, userId]
      );
    }

    // Initialize Stripe with secret key from environment
    const stripeInstance = stripe(process.env.STRIPE_SECRET_KEY);

    // Create ephemeral key for the customer
    const ephemeralKey = await stripeInstance.ephemeralKeys.create(
      { customer: customerId },
      { apiVersion: '2024-06-20' }
    );

    // Create PaymentIntent
    const paymentIntent = await stripeInstance.paymentIntents.create({
      amount: Math.round(amountCad * 100), // Convert to cents
      currency: currency.toLowerCase(),
      customer: customerId,
      automatic_payment_methods: {
        enabled: true
      },
      metadata: {
        parcelId: parcelId.toString(),
        userId: userId.toString()
      }
    });

    // Create payment record in database
    await pool.query(
      'INSERT INTO payments (parcel_id, user_id, amount, currency, payment_method, stripe_payment_intent_id, stripe_customer_id, statut) VALUES ($1, $2, $3, $4, $5, $6, $7, $8)',
      [parcelId, userId, amountCad, currency, 'STRIPE_CARD', paymentIntent.id, customerId, 'PENDING']
    );

    // Return all required keys
    res.json({
      paymentIntentClientSecret: paymentIntent.client_secret,
      ephemeralKeySecret: ephemeralKey.secret,
      customerId: customerId,
      publishableKey: process.env.STRIPE_PUBLISHABLE_KEY
    });

  } catch (error) {
    console.error('Payment intent creation error:', error);
    res.status(500).json({ 
      error: 'Failed to create payment intent',
      message: error.message 
    });
  }
}
