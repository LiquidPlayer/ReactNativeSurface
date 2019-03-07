/* Dummy js asset.  Will run after binding is complete, but before
 * the Promise is resolved.  Good place to add polyfill as needed.
 */
delete global.GLOBAL
global.GLOBAL = global;
