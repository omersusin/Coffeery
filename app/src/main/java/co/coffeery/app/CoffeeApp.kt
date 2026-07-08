package co.coffeery.app

import android.app.Application

/** Application entry point. Room + repository are created lazily on first use
 *  (see AppDatabase.get / CoffeeRepository), so no eager work happens here. */
class CoffeeApp : Application()
