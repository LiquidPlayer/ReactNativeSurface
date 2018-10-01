import './index.js';
import {name as appName} from './app.json';

const RNS = 'org.liquidplayer.surface.reactnative.ReactNativeSurface';
LiquidCore.attach(RNS, (error) => {
    console.log('In attach callback');
    if (error) console.error(error);
    LiquidCore.reactnative.startReactApplication(appName);
});
