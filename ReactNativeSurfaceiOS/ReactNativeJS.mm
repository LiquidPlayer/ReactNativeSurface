//
//  ReactNativeJS.m
//  caraml-console
//
//  Created by Eric Lange on 3/4/19.
//

#import "ReactNativeJS.h"
#import "LCReactNativeView.h"

typedef enum _State {
    Init,
    Attaching,
    Attached,
    Detaching,
    Detached
} State;

@interface ReactNativeJS()
@property (nonatomic, strong) JSValue* thiz;
@property (nonatomic, strong, readonly) JSValue* emitFunc;
@end

@implementation ReactNativeJS {
    State currentState_;
    JSValue* attachPromise_;
    JSValue* detachPromise_;
    LCReactNativeView* currentView_;
    BOOL processedException_;
    LCProcess *process_;
    JSContext *context_;
    JSValue *emitFunc_;
    LCCaramlJS *caramlJS_;
}

static NSString* createJSS =
@"((thiz) => {"
@"  let e=new events();"
@"  e.attach = (o)=>thiz.attach(o);"
@"  e.detach = ()=>thiz.detach();"
@"  e.state  = ()=>thiz.state();"
@"  e.write  = (s)=>thiz.write(s);"
@"  return e;"
@"})";

static NSString* createPromiseObjectS =
@"(()=>{"
@"  var po = {}; var clock = true;"
@"  var timer = setInterval(()=>{if(!clock) clearTimeout(timer);}, 100); "
@"  po.promise = new Promise((resolve,reject)=>{po.resolve=resolve;po.reject=reject});"
@"  po.promise.then(()=>{clock=false}).catch(()=>{clock=false});"
@"  return po;"
@"})()";

static NSString* emitFuncS = @"(()=>{return function(){return arguments[0].emit.call(...arguments)}})()";

- (instancetype) init:(JSContext*)context
              service:(LCMicroService*)service
                 opts:(JSValue*)opts
              binding:(JSValue*)binding
{
    self = [super init];
    if (self != nil) {
        currentState_ = Init;
        process_ = service.process;
        context_ = context;

        JSValue *promise = [context evaluateScript:createPromiseObjectS];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            self->currentView_ = [[LCReactNativeView alloc] initWithFrame:CGRectMake(0, 0, 0, 0)];
            [self->currentView_ setSession:self];
            [self->currentView_ bind:service export:binding config:opts onBound:^{
                self->currentState_ = Detached;
                [self->process_ async:^(JSContext *context) {
                    [self.emitFunc callWithArguments:@[self.thiz, @"ready"]];
                    [promise[@"resolve"] callWithArguments:@[]];
                }];
            } onError:^(NSString* message) {
                [self->process_ async:^(JSContext *context) {
                    [self.emitFunc callWithArguments:@[self.thiz, @"error", message]];
                    [promise[@"reject"] callWithArguments:@[message]];
                }];
            }];
        });
    }
    return self;
}

- (JSValue *)emitFunc
{
    if (emitFunc_ == nil) {
        emitFunc_ = [context_ evaluateScript:emitFuncS];
    }
    assert(emitFunc_ != nil && emitFunc_.isObject);
    return emitFunc_;
}

+ (JSValue *) createReact:(JSContext*)context
                  service:(LCMicroService*)service
                     opts:(JSValue*)opts
                  binding:(JSValue*)binding
{
    ReactNativeJS *reactJS = [[ReactNativeJS alloc] init:context service:service opts:opts binding:binding];
    JSValue *createJS = [context evaluateScript:createJSS];
    JSValue *nativeObject = [createJS callWithArguments:@[reactJS]];
    reactJS.thiz = nativeObject;
    return nativeObject;
}

#pragma JavaScript API

- (JSValue*) attach:(JSValue*)value
{
    attachPromise_ = [context_ evaluateScript:createPromiseObjectS];
    @try {
        if (value == nil || !value.isObject)
            @throw [NSException exceptionWithName:@"attach: first argument must be a caraml object"
                                           reason:nil userInfo:nil];
        if (currentState_ != Detached)
            @throw [NSException exceptionWithName:@"attach: must be in detached state"
                                           reason:nil userInfo:nil];
        caramlJS_ = [LCCaramlJS from:value];
        currentState_ = Attaching;
        [caramlJS_ attach:self];
    } @catch (NSException* e) {
        [self onError:e];
    }
    return attachPromise_[@"promise"];
}

- (NSString*) state
{
    switch (currentState_) {
        case Init:      return @"init";
        case Attaching: return @"attaching";
        case Attached:  return @"attached";
        case Detaching: return @"detaching";
        case Detached:  return @"detached";
    }
    assert(0);
}

- (JSValue *) detach
{
    detachPromise_ = [context_ evaluateScript:createPromiseObjectS];
    JSValue *promise = detachPromise_[@"promise"];
    if (currentState_ == Detached) {
        [self.emitFunc callWithArguments:@[self.thiz, @"detached"]];
        [detachPromise_[@"resolve"] callWithArguments:@[]];
        detachPromise_ = nil;
    } else if (currentState_ == Attached) {
        assert(caramlJS_ != nil);
        currentState_ = Detaching;
        [caramlJS_ detach];
    } else {
        [detachPromise_[@"reject"] callWithArguments:@[@"Attach/detach pending"]];
        detachPromise_ = nil;
    }
    return promise;
}

#pragma LCCaramlSurface

- (UIView*) getView
{
    return currentView_;
}

- (void) onAttached:(BOOL)fromRestore
{
    currentState_ = Attached;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [self->currentView_ attach:^{
            [self->process_ async:^(JSContext *context) {
                if (!fromRestore) {
                    if (self->attachPromise_ != nil) {
                        [self->attachPromise_[@"resolve"] callWithArguments:@[]];
                    }
                    [self.emitFunc callWithArguments:@[self.thiz, @"attached"]];
                    self->attachPromise_ = nil;
                }
            }];
        }];
    });
}

- (void) onDetached
{
    currentState_ = Detached;
    if (currentView_ != nil) [currentView_ detach];
    caramlJS_ = nil;
    
    [process_ async:^(JSContext *context) {
        [self.emitFunc callWithArguments:@[self.thiz, @"detached"]];
        if (self->detachPromise_ != nil) {
            [self->detachPromise_[@"resolve"] callWithArguments:@[]];
        }
        self->detachPromise_ = nil;
    }];
}

- (void) onError:(NSException*) e
{
    if (currentState_ != Init) {
        currentState_ = Detached;
    }
    JSValue *promise = attachPromise_ ? attachPromise_ : detachPromise_;
    [process_ async:^(JSContext *context) {
        if (promise != nil) {
            [promise[@"reject"] callWithArguments:@[e.name]];
        }
        [self.emitFunc callWithArguments:@[self.thiz, @"error", e.name]];
    }];
    attachPromise_ = detachPromise_ = nil;
}


@end
