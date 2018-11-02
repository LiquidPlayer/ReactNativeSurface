'use strict';

require('react-native/setupBabel')();

const Metro = require('metro');
const config = require('react-native/local-cli/core');
const runServer = require('react-native/local-cli/server/runServer');

var args = {
  assetExts: [],
  host: "",
  platforms: config.getPlatforms(),
  port: 8081,
  projectRoots: config.getProjectRoots(),
  resetCache: false,
  sourceExts: config.getSourceExts(),
  verbose: false,
};

const startedCallback = logReporter => {
  logReporter.update({
    type: 'initialize_started',
    port: args.port,
    projectRoots: args.projectRoots,
  });
};

const readyCallback = logReporter => {
  logReporter.update({
    type: 'initialize_done',
  });
};

config.getModulesRunBeforeMainModule = () => [];

runServer(args, config, startedCallback, readyCallback);
