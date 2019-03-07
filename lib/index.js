/*
 * Copyright (c) 2019 Eric Lange
 *
 * Distributed under the MIT License.  See LICENSE.md at
 * https://github.com/LiquidPlayer/ReactNativeSurface for terms and conditions.
 */
const bindings = require('bindings')
const native = bindings('react-native')
const events = require('events')

class React extends events {
  constructor(opts) {
    super()
    opts = opts || {}

    let c = native.React(opts)
    let parent = undefined
    let pending = false

    c.once('ready', () => {
      require('react-native/Libraries/Core/InitializeCore')
      this.emit('ready')
    })

    c.on('detached', () => {
      parent = undefined
      this.emit('detached')
    })

    c.on('attached', () => {
      this.emit('attached')
    })

    c.on('error', (e) => {
      this.emit('error', e)
    })

    this.display = (caramlview) => {
      return new Promise((resolve, reject) => {
        const attach = () => {
          parent = caramlview
          pending = false
          c.attach(caramlview).then(resolve).catch(reject)
        }
        const detach = () => {
          c.detach()
        }

        if (!pending) {
          if (caramlview === undefined) caramlview = require('@liquidcore/caraml-core')
          let state = c.state()
          if (state == 'init') {
            pending = true
            this.once('ready', attach)
          } else if (state == 'detaching') {
            pending = true
            c.once('detached', attach)
          } else if (state == 'attaching' || state == 'attached') {
            if (caramlview != parent) {
              pending = true
              if (state == 'attaching') {
                c.once('attached', detach)
              }
              c.once('detached', attach)
            } else {
              // do nothing because we are already attaching/attached to parent
              resolve()
            }
          } else {
            // state == 'detached'
            attach()
          }
        } else {
          reject('A display request is already pending')
        }
      })
    }

    this.start = (appName) => {
      if (c.state() != 'attached') {
        throw new Error('React surface must be attached first')
      }
      native.startReactApplication(appName)
    }

    this.hide = () => {
      return new Promise((resolve,reject) => {
        let state = c.state()
        let onAttached = () => {
          c.detach().then(onDetached).catch(onError)
        }
        let onDetached = () => {
          c.removeListener('error', onError)
          resolve()
        }
        let onError = (e) => {
          c.removeListener('error', onError)
          c.removeListener('attached', onAttached)
          reject(e)
        }
        if (pending) {
          c.once('attached', onAttached)
          c.once('error', onError)
        } else if (parent !== undefined) {
          c.once('error', onError)
          onAttached()
        } else {
          reject('This console is not being displayed')
        }
      })
    }

    this.getParent = () => parent

    this.getState = () => c.state()
  }
}

let singleton

function Factory(opts) {
  if (singleton === undefined) {
    singleton = new React(opts)
  }
  return singleton
}

module.exports = Factory