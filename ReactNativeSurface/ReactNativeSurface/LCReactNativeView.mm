//
//  LCReactNativeView.m
//  ReactNativeSurface
//
//  Created by Eric Lange on 8/31/18.
//  Copyright © 2018 LiquidPlayer. All rights reserved.
//

#import "LCReactRootView.h"
#import "React/RCTRootViewDelegate.h"
#import "LCReactNativeView.h"
#import "cxxreact/JSCExecutor.h"
#import "LiquidCore/API/Process.h"

@interface RCTSource()
{
@public
    NSURL *_url;
    NSData *_data;
    NSUInteger _length;
    NSInteger _filesChangedCount;
}

@end

static RCTSource *RCTSourceCreate(NSURL *url, NSData *data, int64_t length) NS_RETURNS_RETAINED
{
    RCTSource *source = [RCTSource new];
    source->_url = url;
    source->_data = data;
    source->_length = length;
    source->_filesChangedCount = RCTSourceFilesChangedCountNotBuiltByBundler;
    return source;
}

@implementation LCReactNativeView
{
    JSContext* jsContext_;
    LCReactRootView *rootView_;
    LCMicroService *service_;
    RCTBridge* attachedBridge_;
    LCOnSuccessHandler attachedHandler_;
    LCOnSuccessHandler boundHandler_;
}

+ (NSString*) SURFACE_CANONICAL_NAME
{
    return @"org.liquidplayer.surface.reactnative.ReactNativeSurface";
}

+ (NSString*) SURFACE_VERSION
{
    NSDictionary *infoDictionary = [[NSBundle bundleForClass:LCReactNativeView.class]infoDictionary];
    NSString *version = [infoDictionary objectForKey:@"CFBundleShortVersionString"];
    
    return version;
}

- (instancetype) initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
    }
    return self;
}

- (instancetype) initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
    }
    return self;
}

#pragma - LCSurface

- (void) bind:(LCMicroService*)service
       export:(JSValue *)exportObject
       config:(JSValue *)config
      onBound:(LCOnSuccessHandler)onBound
      onError:(LCOnFailHandler)onError;
{
    service_ = service;
    boundHandler_ = onBound;
    [service.process async:^(JSContext *context) {
        self->jsContext_ = context;
        NSThread *jsThread = [NSThread currentThread];
        
        auto startReactApplication = ^(NSString* moduleName) {
            NSLog(@"LiquidCore: startReactApplication (%@)", moduleName);
            [self->rootView_ runApplicationFromJavaScript:self->attachedBridge_ moduleName:moduleName];
        };
        exportObject[@"startReactApplication"] = startReactApplication;

        /*
         * ReactNative tries to overwrite global.console, but Node creates it with the read only
         * attribute set.  So we have to delete it first to remove the attribute.
         */
        JSValue *c = context[@"global"][@"console"];
        [context[@"global"] deleteProperty:@"console"];
        context[@"global"][@"console"] = c;
        
        dispatch_async(dispatch_get_main_queue(), ^{
            NSDictionary *launchOptions = @{ @"jsThread": jsThread };
            RCTBridge *bridge = [[RCTBridge alloc] initWithDelegate:self launchOptions:launchOptions];
            self->rootView_ = [[LCReactRootView alloc] initWithBridge:bridge moduleName:@"LiquidCore" initialProperties:nil];
            self->rootView_.lc_delegate = self;
            [self addSubview:self->rootView_];
            self->rootView_.frame = CGRectMake(0,0,self.frame.size.width,self.frame.size.height);
        });
    }];
}

- (void) layoutSubviews
{
    if (rootView_ != nil) {
        rootView_.frame = CGRectMake(0,0,self.frame.size.width,self.frame.size.height);
    }
}

- (UIView<LCSurface>*) attach:(LCMicroService*)service
                   onAttached:(LCOnSuccessHandler)onAttachedHandler
                      onError:(LCOnFailHandler)onError;
{
    if (attachedBridge_ != nil) {
        onAttachedHandler();
    } else {
        attachedHandler_ = onAttachedHandler;
    }
    return self;
}

- (void) detach
{
}

- (void) reset
{
    
}

#pragma RCTBridgeDelegate

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
    return service_.serviceURI;
}

- (void)loadSourceForBridge:(RCTBridge *)bridge
                  withBlock:(RCTSourceLoadBlock)loadCallback
{
    [service_.process async:^(JSContext *context) {
        NSString *deferred_fn = [[NSUUID UUID] UUIDString];
        __weak JSContext* jsContext = context;
        context[deferred_fn] = ^{
            [jsContext.globalObject deleteProperty:deferred_fn];
            self->boundHandler_();
            self->boundHandler_ = nil;
        };
        
        NSString *code = [NSString stringWithFormat:@"global['%@']();", deferred_fn];
        NSURL* url = [self sourceURLForBridge:bridge];
        NSData *data = [code dataUsingEncoding:NSUTF8StringEncoding];
        RCTSource *source = RCTSourceCreate(url, data, data.length);
        loadCallback(nil, source);
    }];
}

#pragma RCTCxxBridgeDelegate

- (std::unique_ptr<facebook::react::JSExecutorFactory>)jsExecutorFactoryForBridge:(RCTBridge *)bridge
{
    NSString *deviceName = [[UIDevice currentDevice] name];
    NSString *appName = [[NSBundle mainBundle] bundleIdentifier];

    char sjsContext[32];
    sprintf(sjsContext, "%lud", reinterpret_cast<unsigned long>(jsContext_.JSGlobalContextRef));

    auto factory = new facebook::react::JSCExecutorFactory(folly::dynamic::object
       ("OwnerIdentity", "ReactNative")
       ("AppIdentity", [(appName ?: @"unknown") UTF8String])
       ("DeviceIdentity", [(deviceName ?: @"unknown") UTF8String])
       ("UseCustomJSC", false)
       ("UseContext", sjsContext)
#if RCT_PROFILE
//       ("StartSamplingProfilerOnInit", (bool)bridge.devSettings.startSamplingProfilerOnLaunch)
#endif
    );
    return std::unique_ptr<facebook::react::JSExecutorFactory>(factory);
}

#pragma LCReactRootViewDelegate

- (void)onBundleLoaded:(RCTBridge *)bridge
{
    attachedBridge_ = bridge;
    if (attachedHandler_ != nil) {
        attachedHandler_();
        attachedHandler_ = nil;
    }
}

@end

__attribute__((constructor))
void RNstaticInitMethod()
{
    [LCSurfaceRegistration registerSurface:LCReactNativeView.class];
}
