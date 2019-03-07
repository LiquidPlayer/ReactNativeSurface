//
//  ReactNativeJS.h
//  caraml-console
//
//  Created by Eric Lange on 3/4/19.
//

#import <Foundation/Foundation.h>
#import <JavaScriptCore/JavaScriptCore.h>
#import <LiquidCore/LiquidCore.h>
#import <caraml_core/caraml_core.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ReactNativeJSExports<JSExport>
- (JSValue*) attach:(JSValue*)value;
- (NSString*) state;
- (JSValue*) detach;
@end

@interface ReactNativeJS : NSObject<ReactNativeJSExports, LCCaramlSurface>

+ (JSValue*) createReact:(JSContext*)context
                 service:(LCMicroService*)service
                    opts:(JSValue*)opts
                 binding:(JSValue*)binding;

@end

NS_ASSUME_NONNULL_END
