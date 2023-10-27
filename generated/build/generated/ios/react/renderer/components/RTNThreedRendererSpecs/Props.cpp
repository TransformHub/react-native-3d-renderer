
/**
 * This code was generated by [react-native-codegen](https://www.npmjs.com/package/react-native-codegen).
 *
 * Do not edit this file as changes may cause incorrect behavior and will be lost
 * once the code is regenerated.
 *
 * @generated by codegen project: GeneratePropsCpp.js
 */

#include <react/renderer/components/RTNThreedRendererSpecs/Props.h>
#include <react/renderer/core/PropsParserContext.h>
#include <react/renderer/core/propsConversions.h>

namespace facebook {
namespace react {

RTNThreedRendererProps::RTNThreedRendererProps(
    const PropsParserContext &context,
    const RTNThreedRendererProps &sourceProps,
    const RawProps &rawProps): ViewProps(context, sourceProps, rawProps),

    url(convertRawProp(context, rawProps, "url", sourceProps.url, {})),
    fileNameWithExtension(convertRawProp(context, rawProps, "fileNameWithExtension", sourceProps.fileNameWithExtension, {})),
    animationCount(convertRawProp(context, rawProps, "animationCount", sourceProps.animationCount, {0}))
      {}

} // namespace react
} // namespace facebook