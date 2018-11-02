import {name as appName} from './app.json';

const RNS = 'org.liquidplayer.surface.reactnative.ReactNativeSurface'
const RN_CONFIG = { dev : false };

// Don't import or require the React Native core at the global scope.  We
// must set up the bindings first!

const init_react = (surface) => {
    // Initialize React Native core
    require('react-native/Libraries/Core/InitializeCore')
    
    // Register React Native micro-app
    require('./index')
    
    // Attach the surface to our UI
    return surface.attach()
}

const start_microapp = (surface) => {
    // Start the micro app!
    surface.startReactApplication(appName)
    
    return Promise.resolve()
}

LiquidCore
    .bind(RNS, RN_CONFIG)
    .then(init_react)
    .then(start_microapp)
    .then(() => { 
        console.log('React Native micro app is running!')
    })
    .catch((error) => {
        console.error('React Native micro app failed to start.  Reason:')
        console.error(error);
    })
