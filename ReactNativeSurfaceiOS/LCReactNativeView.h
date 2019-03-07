//
//  LCReactNativeView.h
//  ReactNativeSurface
//
//  Created by Eric Lange on 8/31/18.
//  Copyright © 2018 LiquidPlayer. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <LiquidCore/LiquidCore.h>
#import <React/RCTCxxBridgeDelegate.h>
#import "ReactNativeJS.h"
#import "LCReactRootView.h"

typedef void (^LCOnSuccessHandler)(void);
typedef void (^LCOnFailHandler)(NSString *errorMessage);

@interface LCReactNativeView : UIView <RCTCxxBridgeDelegate, LCReactRootViewDelegate>

@property (nonatomic, strong) ReactNativeJS* session;

- (void) bind:(LCMicroService*)service
       export:(JSValue *)exportObject
       config:(JSValue *)config
      onBound:(LCOnSuccessHandler)onBound
      onError:(LCOnFailHandler)onError;

- (void) attach:(LCOnSuccessHandler)onAttachedHandler;

- (void) detach;

@end
