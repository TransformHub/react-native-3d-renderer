import type {ViewProps} from 'ViewPropTypes';
import type {HostComponent} from 'react-native';
import {Int32} from 'react-native/Libraries/Types/CodegenTypes';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

export interface NativeProps extends ViewProps {
  url: string;
  fileNameWithExtension: string;
  animationCount: Int32;
}

export default codegenNativeComponent<NativeProps>(
  'RTNThreedRenderer',
) as HostComponent<NativeProps>;
