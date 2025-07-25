(ns ludovico.frontend.midi
  (:require
    [cljs.core.match :refer-macros [match]]
    [dommy.core :refer-macros [sel sel1]]
    ))

; https://soundprogramming.net/file-formats/general-midi-instrument-list
; https://github.com/danigb/soundfont-player/blob/master/INSTRUMENTS.md
(def instruments [
                  ; Piano
                  "acoustic_grand_piano"                    ; 0
                  "acoustic_grand_piano"                    ; 1
                  "bright_acoustic_piano"                   ; 2
                  "electric_grand_piano"                    ; 3
                  "honkytonk_piano"                         ; 4
                  "electric_piano_1"                        ; 5
                  "electric_piano_2"                        ; 6
                  "harpsichord"                             ; 7
                  "clavinet"                                ; 8
                  ; Chromatic Percussion:
                  "celesta"                                 ; 9
                  "glockenspiel"                            ; 10
                  "music_box"                               ; 11
                  "vibraphone"                              ; 12
                  "marimba"                                 ; 13
                  "xylophone"                               ; 14
                  "tubular_bells"                           ; 15
                  "dulcimer"                                ; 16
                  ; Organ
                  "drawbar_organ"                           ; 17
                  "percussive_organ"                        ; 18
                  "rock_organ"                              ; 19
                  "church_organ"                            ; 20
                  "reed_organ"                              ; 21
                  "accordion"                               ; 22
                  "harmonica"                               ; 23
                  "tango_accordion"                         ; 24
                  ; Guitar
                  "acoustic_guitar_nylon"                   ; 25
                  "acoustic_guitar_steel"                   ; 26
                  "electric_guitar_jazz"                    ; 27
                  "electric_guitar_clean"                   ; 28
                  "electric_guitar_muted"                   ; 29
                  "overdriven_guitar"                       ; 30
                  "distortion_guitar"                       ; 31
                  "guitar_harmonics"                        ; 32
                  ; Bass
                  "acoustic_bass"                           ; 33
                  "electric_bass_finger"                    ; 34
                  "electric_bass_pick"                      ; 35
                  "fretless_bass"                           ; 36
                  "slap_bass_1"                             ; 37
                  "slap_bass_2"                             ; 38
                  "synth_bass_1"                            ; 39
                  "synth_bass_2"                            ; 40
                  ; Strings
                  "violin"                                  ; 41
                  "viola"                                   ; 42
                  "cello"                                   ; 43
                  "contrabass"                              ; 44
                  "tremolo_strings"                         ; 45
                  "pizzicato_strings"                       ; 46
                  "orchestral_harp"                         ; 47
                  "timpani"                                 ; 48
                  "string_ensemble_1"                       ; 49
                  "string_ensemble_2"                       ; 50
                  "synth_strings_1"                         ; 51
                  "synth_strings_2"                         ; 52
                  "choir_aahs"                              ; 53
                  "voice_oohs"                              ; 54
                  "synth_voice"                             ; 55
                  "orchestra_hit"                           ; 56
                  ; Brass
                  "trumpet"                                 ; 57
                  "trombone"                                ; 58
                  "tuba"                                    ; 59
                  "muted_trumpet"                           ; 60
                  "french_horn"                             ; 61
                  "brass_section"                           ; 62
                  "synth_brass_1"                           ; 63
                  "synth_brass_2"                           ; 64
                  ; Reed
                  "soprano_sax"                             ; 65
                  "alto_sax"                                ; 66
                  "tenor_sax"                               ; 67
                  "baritone_sax"                            ; 68
                  "oboe"                                    ; 69
                  "english_horn"                            ; 70
                  "bassoon"                                 ; 71
                  "clarinet"                                ; 72
                  ; Pipe
                  "piccolo"                                 ; 73
                  "flute"                                   ; 74
                  "recorder"                                ; 75
                  "pan_flute"                               ; 76
                  "blown_bottle"                            ; 77
                  "shakuhachi"                              ; 78
                  "whistle"                                 ; 79
                  "ocarina"                                 ; 80
                  ; Synth Lead
                  "lead_1_square"                           ; 81
                  "lead_2_sawtooth"                         ; 82
                  "lead_3_calliope"                         ; 83
                  "lead_4_chiff"                            ; 84
                  "lead_5_charang"                          ; 85
                  "lead_6_voice"                            ; 86
                  "lead_7_fifths"                           ; 87
                  "lead_8_bass__lead"                       ; 88
                  ; Synth Pad
                  "pad_1_new_age"                           ; 89
                  "pad_2_warm"                              ; 90
                  "pad_3_polysynth"                         ; 91
                  "pad_4_choir"                             ; 92
                  "pad_5_bowed"                             ; 93
                  "pad_6_metallic"                          ; 94
                  "pad_7_halo"                              ; 95
                  "pad_8_sweep"                             ; 96
                  ; Synth Effects
                  "fx_1_rain"                               ; 97
                  "fx_2_soundtrack"                         ; 98
                  "fx_3_crystal"                            ; 99
                  "fx_4_atmosphere"                         ; 100
                  "fx_5_brightness"                         ; 101
                  "fx_6_goblins"                            ; 102
                  "fx_7_echoes"                             ; 103
                  "fx_8_sci_fi"                             ; 104
                  ; Ethnic
                  "sitar"                                   ; 105
                  "banjo"                                   ; 106
                  "shamisen"                                ; 107
                  "koto"                                    ; 108
                  "kalimba"                                 ; 109
                  "bag_pipe"                                ; 110
                  "fiddle"                                  ; 111
                  "shanai"                                  ; 112
                  "Percussive"
                  "tinkle_bell"                             ; 113
                  "agogo"                                   ; 114
                  "steel_drums"                             ; 115
                  "woodblock"                               ; 116
                  "taiko_drum"                              ; 117
                  "melodic_tom"                             ; 118
                  "synth_drum"                              ; 119
                  ; Sound Effects
                  "reverse_cymbal"                          ; 120
                  "guitar_fret_noise"                       ; 121
                  "breath_noise"                            ; 122
                  "seashore"                                ; 123
                  "bird_tweet"                              ; 124
                  "telephone_ring"                          ; 125
                  "helicopter"                              ; 126
                  "applause"                                ; 127
                  "gunshot"                                 ; 128
                  ])