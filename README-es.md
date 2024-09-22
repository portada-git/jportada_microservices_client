# PAPI CLI

([Leer en inglés](README.md))

Esta es una aplicación cliente para ejecutar la API de PortADa (PAPI - Portada Application Programming Inyterface) en su primera fase: el tratamiento de imágenes de prensa escrita para mejorar su calidad en relación a la obtención del texto contenido en ellas a través de un proceso OCR, i la obtención del texto a partir de dicho proceso. Se trata de una aplicación de interfaz gráfica que permite realizar 4 tipos de tratamientos a las imagenes: 1) eliminación de defectos gràficos, 2) Remaquetación de las imagenes de texto eliminando el columnado y asegurando un orden secuencial correcto des de arrriba hasta el final de la página, 3) obtención del texto contenido en la imagen a partis de un proceso OCR usan

## Descripción de la aplicación

La aplicación se divide en 4 pestañas, cada una específica para un tratamiento. Todas las pestañas disponen de dos campos llamados __input__ y __output__ donde se debe especificar la carpeta donde se encuentran los documentos (imágenes, json, etc.) a tratar i la carpeta donde guardar los resultados (documentos tratados o json de respuesta). También disponen de dos botones llamados __abrir dialogo__ (__open dialog__ en inglés) para seleccionar las carpetas de entrada y salida

La primera pestaña llamada __Corregir imagenes__ (__Fix images__ en inglés)

Este módulo dispone de dos funciones bàsicas para comprobar y corregir la inclinación de las líneas de texto en una imagen a fin de conseguir texto alineado horizontalmente. Las funciones son:

- ___isSkimageSkewed(skimage, min_angle=0)___. Esta función evalua si la imagen pasada como paràmetro (skimage) corresponde a un documento de líneas de texto, las cuales presentan una inclinación mayor que el valor ángula pasado mediante el parámetro llamado _min_angle_. 
- ___deskewSkimage(skimage)___. La función _deskewSkimage_ corrige la inclinación de las líneas de texto de la imagen pasada como parámetro a fin de conseguir que que estas se visualicen horizontales (sin inclinación). 

Ambas funciones precisan recibir una imagen compatible con la cargada por skimage.io 

