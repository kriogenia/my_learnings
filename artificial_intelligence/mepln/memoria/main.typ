#import "@preview/charged-ieee:0.1.3": ieee

#set text(
  lang: "es"
)

#show: ieee.with(
  title: [PRÁCTICA 1: Entrenamiento y Evaluación de PoS Taggers y Parsers],
  abstract: [ 
    Memoria de la Práctica 1 de la asignatura Métodos Empíricos de Procesamiento del Lenguaje Natural del Máster de Investigación en Inteligencia Artificial de la Universidad Internacional Menéndez Pelayo.
    Curso 2024/25. Cubre el apartado Entregable del Ejercicio 1. TODO
  ],
  authors: (
    (
      name: "Ricardo Soto Estévez",
      department: [Métodos Empíricos de Procesamiento del Lenguaje Natural],
      organization: [Universidad Internacional Menéndez Pelayo],
      location: [Barcelona, España],
      email: "100012973@alumnos.uimp.es"
    ),
  ),
  bibliography: bibliography("refs.bib"),
  figure-supplement: [Fig.],
)

= Ejercicio 1. Entrenamiento y etiquetación con PoS Tagggers

== Con modelos pre-entrenados

=== Inglés

El primer idioma a tratar en este ejercicio es el inglés. El etiquetador elegido fue el _Illinois Part of Speech Tagger_@RothZe98@Even-ZoharRo01, en su versión 2.0.2 obtenido desde la página web oficial#footnote[https://cogcomp.seas.upenn.edu/page/download_view/POS].

El _Illinois PoS Tagger_ usa una arquitectura _SNOW_ (Sparse Network of Linear Separators) con el algoritmo de aprendizaje Winnow, lo que lo convierte en un etiquetador basado en redes neuronales. Sus nodos de entrada se corresponden con nueve características basadas en la palabra a computar, sus dos palabras anteriores y las dos posteriores. En su salida produce la etiqueta, que es inmediatamente aplicada a la palabra, por lo que las palabras subsiguientes dependerán en la etiquetación de las anteriores.

El libro _The Advancement of Learning_ de Francis Bacon fue seleccionado para la ejecución del tagger. Se descargó su versión de texto plano de _Project Gutenberg_#footnote[https://www.gutenberg.org/ebooks/5500] y el único preprocesamiento realizado fue la eliminación del prefacio y el posfacio includos por _Project Gutenberg_ con la metadata y la licencia de uso. El conteo de palabras total del texto resultante fue de $83821$.

Tras la ejecución del etiquetador se obtuvo una salida con la misma estructura del texto introducido, con la diferencia de que cada token reconocido por el tagger se convierte en una tupla delimitada entre paréntesis. En esta tupla el primer elemento de la misma es la etiqueta generada; y el segundo, separado por un espacio, el token procesado. La página ofrecida en su documentación para consultar el tagset estaba caída en el momento de consulta, pero se puede encontrar la lista de posible etiquetas en la documentación de la librería#footnote[https://cogcomp.seas.upenn.edu/software/doc/apidocs/edu/illinois/cs/cogcomp/lbjava/nlp/POS.html].

=== Lengua romance

Para la segunda lengua se seleccionó el gallego y se eligió el etiquetador _CitiusTagger_#footnote[Disponible para descarga en: http://gramatica.usc.es/pln/tools/CitiusTools.html], desarrollado por el grupo ProLNat\@GE de la Universidad de Santiago de Compostela y presentado en 2014 en la revista de la Sociedad Española para el Procesamiento del Lenguaje Natural@Gamallo2014.

Este desambiguador morfosintático fue entrenado en gallego con un corpus de $237000$ tokens formado por textos periodísticos y académicos; y emplea un lexicon con más de $428000$ lexemas. Es un clasificador de bigramas bayesiano con contexto. La desambiguación de tokens no evidentes la lleva a cabo por medio de la selección de la etiqueta más probable teniendo en cuenta la probabilidad asociada a las etiquetas anterior y posterior, así como la probabilidad de la copresencia del token con cada una de estas dos etiquetas inmediatamente próximas. Su funcionamiento es similar al formalismo de los modelos ocultos de Markov (HMM)@Brants2000, pero con desambiguación token a token en vez de usando el algoritmo Viterbi.

El texto seleccionado fue el artículo de Galicia@wiki:Galicia de la Wikipedia en gallego. Puesto que el texto se encuentra en versión web no es apto para ser analizado directamente con el etiquetador, por lo que se utilizó la TextExtracts API#footnote[Disponible en: https://www.mediawiki.org/wiki/Special:MyLanguage/Extension:TextExtracts] de Wikimedia para obtener una versión con el texto plano#footnote[Petición empleada: https://gl.wikipedia.org/w/api.php?action=query&prop=extracts&exlimit=1&titles=Galicia&explaintext=1&formatversion=2&format=json]. Por medio de sustituciones de expresiones regulares también se eliminó el marcado de las secciones y se comprimieron múltiples líneas vacías. El texto resultante cuenta con $10462$ palabras.

La salida generada por _CitiusTagger_ es un texto plano con una línea dedicada a cada token procesado. En cada una de estas líneas aparecen tres elementos separados por espacios: el primer elemento es el token leído; el segundo es el lema del token leído; y el último elemento es la etiqueta generada para ese token. Dicha etiqueta proviene del tagset para el gallego de FreeLing#footnote[Disponible para consulta en: https://freeling-user-manual.readthedocs.io/en/latest/tagsets/tagset-gl/], que a su vez se basa en EAGLES. Las frases se separan con una línea vacía, y los saltos de línea con dos. Las líneas vacías leídas directamente del texto se representan con el token `<blank>`.

= Methods <sec:methods>
#lorem(45)

$ a + b = gamma $ <eq:gamma>


#figure(
  placement: none,
  circle(radius: 15pt),
  caption: [A circle representing the Sun.]
) <fig:sun>

In @fig:sun you can see a common representation of the Sun, which is a star that is located at the center of the solar system.

#lorem(120)

#figure(
  caption: [The Planets of the Solar System and Their Average Distance from the Sun],
  placement: top,
  table(
    // Table styling is not mandated by the IEEE. Feel free to adjust these
    // settings and potentially move them into a set rule.
    columns: (6em, auto),
    align: (left, right),
    inset: (x: 8pt, y: 4pt),
    stroke: (x, y) => if y <= 1 { (top: 0.5pt) },
    fill: (x, y) => if y > 0 and calc.rem(y, 2) == 0  { rgb("#efefef") },

    table.header[Planet][Distance (million km)],
    [Mercury], [57.9],
    [Venus], [108.2],
    [Earth], [149.6],
    [Mars], [227.9],
    [Jupiter], [778.6],
    [Saturn], [1,433.5],
    [Uranus], [2,872.5],
    [Neptune], [4,495.1],
  )
) <tab:planets>

In @tab:planets, you see the planets of the solar system and their average distance from the Sun.
The distances were calculated with @eq:gamma that we presented in @sec:methods.

