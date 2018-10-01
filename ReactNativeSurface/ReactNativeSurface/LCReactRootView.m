/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import "React/Base/RCTRootViewDelegate.h"

#import <objc/runtime.h>

#import "React/RCTAssert.h"
#import "React/RCTBridge.h"
#import "React/Base/RCTBridge+Private.h"
#import "React/RCTEventDispatcher.h"
#import "React/RCTKeyCommands.h"
#import "React/RCTLog.h"
#import "React/RCTPerformanceLogger.h"
#import "React/RCTProfile.h"
#import "React/Base/RCTRootContentView.h"
#import "React/RCTTouchHandler.h"
#import "React/RCTUIManager.h"
#import "React/RCTUIManagerUtils.h"
#import "React/RCTUtils.h"
#import "React/RCTView.h"
#import "React/UIView+React.h"
#import "LCReactRootView.h"

#if TARGET_OS_TV
#import "React/RCTTVRemoteHandler.h"
#import "React/RCTTVNavigationEventEmitter.h"
#endif

@interface RCTUIManager (LCReactRootView)

- (NSNumber *)allocateRootTag;

@end

@implementation LCReactRootView
{
    RCTBridge *_bridge;
    NSString *_moduleName;
    RCTRootContentView *_contentView;
    BOOL _passThroughTouches;
    CGSize _intrinsicContentSize;
}

- (instancetype)initWithBridge:(RCTBridge *)bridge
                    moduleName:(NSString *)moduleName
             initialProperties:(NSDictionary *)initialProperties
{
    RCTAssertMainQueue();
    RCTAssert(bridge, @"A bridge instance is required to create an RCTRootView");
    RCTAssert(moduleName, @"A moduleName is required to create an RCTRootView");
    
    RCT_PROFILE_BEGIN_EVENT(RCTProfileTagAlways, @"-[RCTRootView init]", nil);
    if (!bridge.isLoading) {
        [bridge.performanceLogger markStartForTag:RCTPLTTI];
    }
    
    if (self = [super initWithFrame:CGRectZero]) {
        self.backgroundColor = [UIColor whiteColor];
        
        _bridge = bridge;
        _moduleName = moduleName;
        _appProperties = [initialProperties copy];
        _loadingViewFadeDelay = 0.25;
        _loadingViewFadeDuration = 0.25;
        _sizeFlexibility = RCTRootViewSizeFlexibilityNone;
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(bridgeDidReload)
                                                     name:RCTJavaScriptWillStartLoadingNotification
                                                   object:_bridge];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(javaScriptDidLoad:)
                                                     name:RCTJavaScriptDidLoadNotification
                                                   object:_bridge];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(hideLoadingView)
                                                     name:RCTContentDidAppearNotification
                                                   object:self];
        
#if TARGET_OS_TV
        self.tvRemoteHandler = [RCTTVRemoteHandler new];
        for (NSString *key in [self.tvRemoteHandler.tvRemoteGestureRecognizers allKeys]) {
            [self addGestureRecognizer:self.tvRemoteHandler.tvRemoteGestureRecognizers[key]];
        }
#endif
        
        [self showLoadingView];
        
        // Immediately schedule the application to be started.
        // (Sometimes actual `_bridge` is already batched bridge here.)
        //    [self bundleFinishedLoading:([_bridge batchedBridge] ?: _bridge)];
    }
    
    RCT_PROFILE_END_EVENT(RCTProfileTagAlways, @"");
    
    return self;
}

- (instancetype)initWithBundleURL:(NSURL *)bundleURL
                       moduleName:(NSString *)moduleName
                initialProperties:(NSDictionary *)initialProperties
                    launchOptions:(NSDictionary *)launchOptions
{
    RCTBridge *bridge = [[RCTBridge alloc] initWithBundleURL:bundleURL
                                              moduleProvider:nil
                                               launchOptions:launchOptions];
    
    return [self initWithBridge:bridge moduleName:moduleName initialProperties:initialProperties];
}

RCT_NOT_IMPLEMENTED(- (instancetype)initWithFrame:(CGRect)frame)
RCT_NOT_IMPLEMENTED(- (instancetype)initWithCoder:(NSCoder *)aDecoder)

#if TARGET_OS_TV
- (UIView *)preferredFocusedView
{
    if (self.reactPreferredFocusedView) {
        return self.reactPreferredFocusedView;
    }
    return [super preferredFocusedView];
}
#endif

#pragma mark - passThroughTouches

- (BOOL)passThroughTouches
{
    return _contentView.passThroughTouches;
}

- (void)setPassThroughTouches:(BOOL)passThroughTouches
{
    _passThroughTouches = passThroughTouches;
    _contentView.passThroughTouches = passThroughTouches;
}

#pragma mark - Layout

- (CGSize)sizeThatFits:(CGSize)size
{
    CGSize fitSize = _intrinsicContentSize;
    CGSize currentSize = self.bounds.size;
    
    // Following the current `size` and current `sizeFlexibility` policy.
    fitSize = CGSizeMake(
                         _sizeFlexibility & RCTRootViewSizeFlexibilityWidth ? fitSize.width : currentSize.width,
                         _sizeFlexibility & RCTRootViewSizeFlexibilityHeight ? fitSize.height : currentSize.height
                         );
    
    // Following the given size constraints.
    fitSize = CGSizeMake(
                         MIN(size.width, fitSize.width),
                         MIN(size.height, fitSize.height)
                         );
    
    return fitSize;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    _contentView.frame = self.bounds;
    _loadingView.center = (CGPoint){
        CGRectGetMidX(self.bounds),
        CGRectGetMidY(self.bounds)
    };
}

- (UIViewController *)reactViewController
{
    return _reactViewController ?: [super reactViewController];
}

- (BOOL)canBecomeFirstResponder
{
    return YES;
}

- (void)setLoadingView:(UIView *)loadingView
{
    _loadingView = loadingView;
    if (!_contentView.contentHasAppeared) {
        [self showLoadingView];
    }
}

- (void)showLoadingView
{
    if (_loadingView && !_contentView.contentHasAppeared) {
        _loadingView.hidden = NO;
        [self addSubview:_loadingView];
    }
}

- (void)hideLoadingView
{
    if (_loadingView.superview == self && _contentView.contentHasAppeared) {
        if (_loadingViewFadeDuration > 0) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(_loadingViewFadeDelay * NSEC_PER_SEC)),
                           dispatch_get_main_queue(), ^{
                               
                               [UIView transitionWithView:self
                                                 duration:self->_loadingViewFadeDuration
                                                  options:UIViewAnimationOptionTransitionCrossDissolve
                                               animations:^{
                                                   self->_loadingView.hidden = YES;
                                               } completion:^(__unused BOOL finished) {
                                                   [self->_loadingView removeFromSuperview];
                                               }];
                           });
        } else {
            _loadingView.hidden = YES;
            [_loadingView removeFromSuperview];
        }
    }
}

- (NSNumber *)reactTag
{
    RCTAssertMainQueue();
    if (!super.reactTag) {
        /**
         * Every root view that is created must have a unique react tag.
         * Numbering of these tags goes from 1, 11, 21, 31, etc
         *
         * NOTE: Since the bridge persists, the RootViews might be reused, so the
         * react tag must be re-assigned every time a new UIManager is created.
         */
        self.reactTag = RCTAllocateRootViewTag();
    }
    return super.reactTag;
}

- (void)bridgeDidReload
{
    RCTAssertMainQueue();
    // Clear the reactTag so it can be re-assigned
    self.reactTag = nil;
}

- (void)javaScriptDidLoad:(NSNotification *)notification
{
    RCTAssertMainQueue();
    
    // Use the (batched) bridge that's sent in the notification payload, so the
    // RCTRootContentView is scoped to the right bridge
    RCTBridge *bridge = notification.userInfo[@"bridge"];
    if (bridge != _contentView.bridge) {
        [self bundleFinishedLoading:bridge];
    }
}

- (void)bundleFinishedLoading:(RCTBridge *)bridge
{
    RCTAssert(bridge != nil, @"Bridge cannot be nil");
    if (!bridge.valid) {
        return;
    }
    
    [_contentView removeFromSuperview];
    _contentView = [[RCTRootContentView alloc] initWithFrame:self.bounds
                                                      bridge:bridge
                                                    reactTag:self.reactTag
                                              sizeFlexiblity:_sizeFlexibility];
    //[self runApplication:bridge];
    if (self.lc_delegate != nil) {
        [self.lc_delegate onBundleLoaded:bridge];
    }
    
    _contentView.passThroughTouches = _passThroughTouches;
    [self insertSubview:_contentView atIndex:0];
    
    if (_sizeFlexibility == RCTRootViewSizeFlexibilityNone) {
        self.intrinsicContentSize = self.bounds.size;
    }
}

- (void)runApplication:(RCTBridge *)bridge
{
    NSString *moduleName = _moduleName ?: @"";
    NSDictionary *appParameters = @{
                                    @"rootTag": _contentView.reactTag,
                                    @"initialProps": _appProperties ?: @{},
                                    };
    
    RCTLogInfo(@"Running application %@ (%@)", moduleName, appParameters);
    [bridge enqueueJSCall:@"AppRegistry"
                   method:@"runApplication"
                     args:@[moduleName, appParameters]
               completion:NULL];
}

- (void)runApplicationFromJavaScript:(RCTBridge *)bridge
                          moduleName:(NSString*)moduleName
{
    NSDictionary *appParameters = @{
                                    @"rootTag": _contentView.reactTag,
                                    @"initialProps": _appProperties ?: @{},
                                    };
    
    RCTLogInfo(@"Running application %@ (%@)", moduleName, appParameters);
    [bridge enqueueJSCall:@"AppRegistry"
                   method:@"runApplication"
                     args:@[moduleName, appParameters]
               completion:NULL];
}

- (void)setSizeFlexibility:(RCTRootViewSizeFlexibility)sizeFlexibility
{
    if (_sizeFlexibility == sizeFlexibility) {
        return;
    }
    
    _sizeFlexibility = sizeFlexibility;
    [self setNeedsLayout];
    _contentView.sizeFlexibility = _sizeFlexibility;
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event
{
    // The root view itself should never receive touches
    UIView *hitView = [super hitTest:point withEvent:event];
    if (self.passThroughTouches && hitView == self) {
        return nil;
    }
    return hitView;
}

- (void)setAppProperties:(NSDictionary *)appProperties
{
    RCTAssertMainQueue();
    
    if ([_appProperties isEqualToDictionary:appProperties]) {
        return;
    }
    
    _appProperties = [appProperties copy];

/*
    if (_contentView && _bridge.valid && !_bridge.loading) {
        [self runApplication:_bridge];
    }
*/
}

- (void)setIntrinsicContentSize:(CGSize)intrinsicContentSize
{
    BOOL oldSizeHasAZeroDimension = _intrinsicContentSize.height == 0 || _intrinsicContentSize.width == 0;
    BOOL newSizeHasAZeroDimension = intrinsicContentSize.height == 0 || intrinsicContentSize.width == 0;
    BOOL bothSizesHaveAZeroDimension = oldSizeHasAZeroDimension && newSizeHasAZeroDimension;
    
    BOOL sizesAreEqual = CGSizeEqualToSize(_intrinsicContentSize, intrinsicContentSize);
    
    _intrinsicContentSize = intrinsicContentSize;
    
    [self invalidateIntrinsicContentSize];
    [self.superview setNeedsLayout];
    
    // Don't notify the delegate if the content remains invisible or its size has not changed
    if (bothSizesHaveAZeroDimension || sizesAreEqual) {
        return;
    }
/*
    [_delegate rootViewDidChangeIntrinsicSize:self];
*/
}

- (CGSize)intrinsicContentSize
{
    return _intrinsicContentSize;
}

- (void)contentViewInvalidated
{
    [_contentView removeFromSuperview];
    _contentView = nil;
    [self showLoadingView];
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [_contentView invalidate];
}

- (void)cancelTouches
{
    [[_contentView touchHandler] cancel];
}

@end