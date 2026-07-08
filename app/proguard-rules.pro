# Coffeery — keep rules.
# Room generates code at build time; no runtime reflection rules needed for the
# entities used here. Keep model classes to be safe if minify is enabled later.
-keep class co.coffeery.app.data.model.** { *; }
