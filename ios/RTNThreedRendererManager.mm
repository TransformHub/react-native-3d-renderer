#import <React/RCTLog.h>
#import <React/RCTUIManager.h>
#import <React/RCTViewManager.h>

@interface RTNThreedRendererManager : RCTViewManager
@end

@implementation RTNThreedRendererManager

RCT_EXPORT_MODULE(RTNThreedRenderer)
RCT_EXPORT_VIEW_PROPERTY(fileNameWithExtension, NSString)
RCT_EXPORT_VIEW_PROPERTY(url, NSString)

@end
