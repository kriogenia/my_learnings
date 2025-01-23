#import "@preview/charged-ieee:0.1.3": ieee

#set text(
  lang: "es"
)

#show: ieee.with(
  title: [PRÁCTICA 1: Entrenamiento y Evaluación de PoS Taggers y Parsers],
  abstract: [ 
    Memoria de la Práctica 1 de la asignatura Métodos Empíricos de Procesamiento del Lenguaje Natural del Máster de Investigación en Inteligencia Artificial de la Universidad Internacional Menéndez Pelayo.
    Curso 2024/25. Cubre los ejercicios: 1.a, TODO
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

El primer idioma a tratar en este ejercicio es el inglés. El etiquetador elegido fue #lorem(24)

=== Lengua romance

Para la segunda lengua se seleccionó el gallego y se eligió el etiquetador _CitiusTagger_#footnote[Disponible para descarga en: http://gramatica.usc.es/pln/tools/CitiusTools.html], desarrollado por el grupo ProLNat\@GE de la Universidad de Santiago de Compostela y presentado en 2014 en la revista de la Sociedad Española para el Procesamiento del Lenguaje Natural@Gamallo2014.

Este desambiguador morfosintático fue entrenado en gallego con un corpus de $237000$ tokens formado por textos periodísticos y académicos; y emplea un lexicon con más de $428000$ lexemas. Es un clasificador de bigramas bayesiano con contexto. La desambiguación de tokens no evidentes la lleva a cabo por medio de la selección de la etiqueta más probable teniendo en cuenta la probabilidad asociada a las etiquetas anterior y posterior, así como la probabilidad de la copresencia del token con cada una de estas dos etiquetas inmediatamente próximas. Su funcionamiento es similar al formalismo de los modelos ocultos de Markov (HMM)@Brants2000, pero con desambiguación token a token en vez de usando el algoritmo Viterbi.

El texto seleccionado fue el artículo de Galicia@wiki:Galicia de la Wikipedia en gallego. Puesto que el texto se encuentra en versión web no es apto para ser analizado directamente con el etiquetador, por lo que se utilizó la TextExtracts API#footnote[Disponible en: https://www.mediawiki.org/wiki/Special:MyLanguage/Extension:TextExtracts] de Wikimedia para obtener una versión con el texto plano#footnote[Petición empleada: https://gl.wikipedia.org/w/api.php?action=query&prop=extracts&exlimit=1&titles=Galicia&explaintext=1&formatversion=2&format=json]. Por medio de sustituciones de expresiones regulares también se eliminó el marcado de las secciones y se comprimieron múltiples líneas vacías. El texto resultante cuenta con $10462$ palabras.

La salida generada por _CitiusTagger_ usa el tagset para el gallego de FreeLing#footnote[Disponible para consulta en: https://freeling-user-manual.readthedocs.io/en/latest/tagsets/tagset-gl/], que a su vez se basa en EAGLES. No se ha contado con una versión del texto anotada por lo que el análisis de la salida fue realizado manualmente.

Un primer error visualizado ya al comienzo es la etiquetación de _"xurídica"_ como un adjetivo, esto no es correcto puesto al ir seguido de _"e lexislativamente"_ estamos ante un caso de _elisión verbal_, por lo que _"xurídica"_ actúa como adverbio aunque sea un adjetivo morfológicamente. Sabiendo que el modelo resuelve los tokens de izquierda a derecha, y que además no evalúa contexto a dos palabras de distancia, es fácil ver la carencia que ha provocado este error.

Otro resultado a analizar es la etiquetación de _/km²_ (extraído de _"hab./km²"_). En una de sus apariciones (línea 443) es categorizado como un adjetivo cualificativo, mientras que en la inmediatamente siguiente (línea 458) es categorizado como un sustantivo común. El contexto previo a ambos es el mismo, sin embargo, la presencia de una preposición a continuación, en vez de una conjunción, ha permitido al modelo desambiguarlo más correctamente.

Más allá de todo esto el resultado contiene un altísimo índice de acierto, incluso a la hora de etiquetar las contracciones tan comunes en el gallego.

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

