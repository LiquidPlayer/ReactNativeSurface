//
//  LCReactNativeView.h
//  ReactNativeSurface
//
//  Created by Eric Lange on 8/31/18.
//  Copyright © 2018 LiquidPlayer. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <LiquidCore/LiquidCore.h>
#import "React/CxxBridge/RCTCxxBridgeDelegate.h"

@interface LCReactNativeView : UIView <LCSurface, RCTCxxBridgeDelegate, LCReactRootViewDelegate>

@end
