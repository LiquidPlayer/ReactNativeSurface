/** @format */
import {AppRegistry} from 'react-native';
import App from './App';
import {name as appName} from './app.json';

AppRegistry.registerComponent(appName, () => App);

const RNS = 'org.liquidplayer.surface.reactnative.ReactNativeSurface';
LiquidCore.attach(RNS, (error) => {
    if (error) console.error(error);
    LiquidCore.reactnative.startReactApplication(appName);
});