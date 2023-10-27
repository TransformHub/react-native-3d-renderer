#import "RTNThreedRenderer.h"

#import <react/renderer/components/RTNThreedRendererSpecs/ComponentDescriptors.h>
#import <react/renderer/components/RTNThreedRendererSpecs/EventEmitters.h>
#import <react/renderer/components/RTNThreedRendererSpecs/Props.h>
#import <react/renderer/components/RTNThreedRendererSpecs/RCTComponentViewHelpers.h>
#import "RCTFabricComponentsPlugins.h"

using namespace facebook::react;

@interface RTNThreedRenderer () <RCTRTNThreedRendererViewProtocol>
@property (nonatomic, strong) NSString *fileName;
@property (nonatomic, strong) NSString *url;
@end

@implementation RTNThreedRenderer {
  UIView *_view;
  SCNView *sceneView;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
  return concreteComponentDescriptorProvider<RTNThreedRendererComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const RTNThreedRendererProps>();
    _props = defaultProps;
    
    _view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height)];
    sceneView = [SCNView new];
    [_view addSubview: sceneView];
    
    sceneView.translatesAutoresizingMaskIntoConstraints = NO;
    [NSLayoutConstraint activateConstraints:@[
      [sceneView.leadingAnchor constraintEqualToAnchor:_view.leadingAnchor],
      [sceneView.topAnchor constraintEqualToAnchor:_view.topAnchor],
      [sceneView.trailingAnchor constraintEqualToAnchor:_view.trailingAnchor],
      [sceneView.bottomAnchor constraintEqualToAnchor:_view.bottomAnchor],
    ]];
    
    self.contentView = _view;
  }
  
  return self;
}


- (void)checkFileExists {
  NSString *path = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)[0];
  NSURL *url = [NSURL fileURLWithPath:path];
  NSURL *pathComponent = [url URLByAppendingPathComponent:self.fileName];
  NSString *filePath = [pathComponent path];
  NSFileManager *fileManager = [NSFileManager defaultManager];
  
  if ([fileManager fileExistsAtPath:filePath]) {
    NSLog(@"FILE AVAILABLE");
    [self loadModel];
  } else {
    [self downloadSceneTask];
  }
}

- (void)downloadSceneTask {
  NSURL *url = [NSURL URLWithString:self.url];
  if (!url) {
    return;
  }
  
  NSURLSession *downloadSession = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration] delegate:self delegateQueue:nil];
  
  NSURLSessionDownloadTask *downloadTask = [downloadSession downloadTaskWithURL:url];
  [downloadTask resume];
}

- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask didFinishDownloadingToURL:(NSURL *)location {
  NSURL *fileURL = [self getDocumentsDirectory];
  fileURL = [fileURL URLByAppendingPathComponent:self.fileName];
  
  NSFileManager *fileManager = [NSFileManager defaultManager];
  
  NSError *error;
  if ([fileManager copyItemAtURL:location toURL:fileURL error:&error]) {
    NSLog(@"Successfully Saved File %@", fileURL);
    [self loadModel];
  } else {
    NSLog(@"Error Saving: %@", error);
    [self loadModel];
  }
}

- (NSURL *)getDocumentsDirectory {
  NSArray<NSURL *> *paths = [[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask];
  return [paths firstObject];
}

- (void)loadModel {
  NSURL *downloadedScenePath = [self getDocumentsDirectory];
  downloadedScenePath = [downloadedScenePath URLByAppendingPathComponent:self.fileName];
  
  sceneView.autoenablesDefaultLighting = YES;
  sceneView.showsStatistics = NO;
  
  MDLAsset *asset = [[MDLAsset alloc] initWithURL:downloadedScenePath];
  [asset loadTextures];
  
  SCNScene *scene = [SCNScene sceneWithMDLAsset:asset];
  
  sceneView.scene = scene;
  sceneView.allowsCameraControl = YES;
}


- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
  const auto &oldViewProps = *std::static_pointer_cast<RTNThreedRendererProps const>(_props);
  const auto &newViewProps = *std::static_pointer_cast<RTNThreedRendererProps const>(props);
  
  if (oldViewProps.url != newViewProps.url) {
    self.url = [NSString stringWithUTF8String:newViewProps.url.c_str()];
  }
  
  if(oldViewProps.fileNameWithExtension != newViewProps.fileNameWithExtension){
    self.fileName = [NSString stringWithUTF8String:newViewProps.fileNameWithExtension.c_str()];
  }
  
  if (self.url.length > 0 && self.fileName.length > 0) {
    [self checkFileExists];
  }
  
  [super updateProps:props oldProps:oldProps];
}


@end

Class<RCTComponentViewProtocol> RTNThreedRendererCls(void)
{
  return RTNThreedRenderer.class;
}
