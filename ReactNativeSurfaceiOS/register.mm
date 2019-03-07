/*
 * Copyright (c) 2019 Eric Lange
 *
 * Distributed under the MIT License.  See LICENSE.md at
 * https://github.com/LiquidPlayer/ReactNativeSurface for terms and conditions.
 */
#import <LiquidCore/LiquidCore.h>
#import "ReactNativeJS.h"
#include "../src/react-native.h"

@interface ReactNative : NSObject<LCAddOn>

@end

@implementation ReactNative

- (id) init
{
    self = [super init];
    if (self != nil) {
        
    }
    return self;
}

- (void) register:(NSString*) module
{
    assert([@"react-native" isEqualToString:module]);
    register_reactnative();
}

- (void) require:(JSValue*) binding service:(LCMicroService *)service
{
    assert(binding != nil);
    assert([binding isObject]);
    
    JSContext *context = [binding context];
    JSValue *boundObject = binding;
    binding[@"React"] = ^(JSValue* opts){
        return [ReactNativeJS createReact:context service:service opts:opts binding:boundObject];
    };
}

@end

@interface ReactNativeFactory : LCAddOnFactory

@end

@implementation ReactNativeFactory

- (id) init
{
    self = [super init];
    if (self) {
        
    }
    return self;
}

- (id<LCAddOn>)createInstance
{
    return [[ReactNative alloc] init];
}

@end

__attribute__((constructor))
static void reactJSRegistration()
{
    [LCAddOnFactory registerAddOnFactory:@"react-native" factory:[[ReactNativeFactory alloc] init]];
}
