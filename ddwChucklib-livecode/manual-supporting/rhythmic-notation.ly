\version "2.18.2"
\language "english"

global = { \numericTimeSignature \time 4/4 }

\markup {
  \column {
    \line {
      \column {
        \line { 1. \typewriter "\"--\"" }
        \hspace #25
      }
      \column {
        \score {
          \new RhythmicStaff {
            \global b2 b2
          }
          \layout {}
        }
      }
    }

    \line {
      \column {
        \line { 2. \typewriter "\"----\"" }
        \hspace #25
      }
      \column {
        \score {
          \new RhythmicStaff {
            \global b4 b b b
          }
          \layout {}
        }
      }
    }

    \line {
      \column {
        \line { 3. \typewriter "\"- --\"" }
        \hspace #25
      }
      \column {
        \score {
          \new RhythmicStaff {
            \global b2 b4 b4
          }
          \layout {}
        }
      }
    }

    \line {
      \column {
        \line { 4. \typewriter "\"- --- -\"" }
        \hspace #25
      }
      \column {
        \score {
          \new RhythmicStaff {
            \global \tuplet 7/4 { b2 b4 b4 b2 b4 }
          }
          \layout {}
        }
      }
    }

    \line {
      \column {
        \line { 5. \typewriter "\"-|-|-|-\"" }
        \hspace #25
      }
      \column {
        \score {
          \new RhythmicStaff {
            \global b4 b b b
          }
          \layout {}
        }
      }
    }

    \line {
      \column {
        \line { 6. \typewriter "\"-|--|-|-\"" }
        \hspace #25
      }
      \column {
        \score {
          \new RhythmicStaff {
            \global b4 b8 b b4 b
          }
          \layout {}
        }
      }
    }

    \line {
      \column {
        % Note: There is a U+200B "zero-width space"
        % between the consecutive spaces in beat 2.
        % Otherwise SVG renders incorrectly.
        \line { 7. \typewriter "\"--| - ​ |- --| -\"" }
        \hspace #25
      }
      \column {
        \score {
          \new RhythmicStaff {
            \global b8 b ~ b16 b8. b8 b16 b16 ~ b8 b8
          }
          \layout {}
        }
      }
    }
  }
}

 