const RNS = 'org.liquidplayer.surface.reactnative.ReactNativeSurface'

// Don't import or require the React Native core at the global scope.  We
// must set up the bindings first!

const init_react = (surface) => {
    // Initialize React Native core
    require('react-native/Libraries/Core/InitializeCore')
    
    // Register React Native micro-app
    require('./RNTesterApp/RNTesterApp')
    
    // Attach the surface to our UI
    return surface.attach()
}

const start_microapp = (surface) => {
    // Start the micro app!
    surface.startReactApplication('RNTesterApp')
    
    return Promise.resolve()
}

LiquidCore
    .bind(RNS)
    .then(init_react)
    .then(start_microapp)
    .then(() => { 
        console.log('React Native micro app is running!')
    })
    .catch((error) => {
        console.error('React Native micro app failed to start.  Reason:')
        console.error(error);
    })
