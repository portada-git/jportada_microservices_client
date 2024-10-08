# PAPI CLI

([Read in Spanish](README-es.md))

Images of printed text may present anomalies such as transparencies on the back, inclined or curved lines, poorly or excessively marked characters, etc. Typically, these defects in image quality tend to negatively interfere with the text recognition process (OCR). Here we present a set of tools to check and correct some of these irregularities in order to improve their quality and thus minimize the error rate during the text recognition process.

## deskew_tools module

This module has three basic functions to check and correct the tilt of text lines in an image in order to achieve horizontally aligned text. The functions are:

- ___isSkimageSkewed(skimage, min_angle=0)___. This function evaluates whether the image passed as a parameter (skimage) corresponds to a document of text lines, which have a greater inclination than the angle value passed through the parameter called _min_angle_.
- ___deskewSkimage(skimage)___. The _deskewSkimage_ function corrects the tilt of the text lines of the image passed as a parameter in order to ensure that they are displayed horizontally (without tilt).
- ___deskewImageFile(input_path, output_path='')___. This function recive two parameters. First one is the path of de image file to deskew and the second one the path where the image deskewed must be saved. This last parameter is optional. If it is not passed the deskewed image will be saved with the same path as the original. 

Both functions require receiving an image compatible with the one uploaded by skimage.io

## dewarp_tools module

This module has two basic functions to check and correct the curved text lines present in an image of textual content, in order to achieve straight and horizontal lines. The functions are:

- ___is_cv2image_curve(cv2img)___. 
- ___dewarp_cv2image(cv2img)___. 
- ___dewarp_cv2image_file(input_path, output_path='')___. 

