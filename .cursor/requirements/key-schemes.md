## KEY SCHEME
|Key Scheme Tag|Notes|
|---|---|
|None/Z| Encryption of a single-length DES key using the ANSI X9.17 method. Used for encrypting keys for local use (under a Variant LMK) or for importing/exporting keys (e.g. under a ZMK).The use of this scheme requires some security settings to be changed. |
|U|Encryption of a double-length DES key using the Thales Variant method. Used for encrypting keys for local use (under a Variant LMK) or for importing/exporting keys (e.g. under a ZMK).The use of this scheme for import/export requires some security settings to be changed.|
| T | Encryption of a triple-length DES key using the Thales Variant method. Used for encrypting keys for local use (under a Variant LMK) or for importing/exporting keys (e.g. under a ZMK).The use of this scheme for import/export requires some security settings to be  changed. |
| X | Encryption of a double-length DES key using the ANSI X9.17 method. Used for encrypting keys for local use (under a Variant LMK) or for importing/exporting keys (e.g. under a ZMK).The use of this scheme requires some security settings to be changed. |
| Y | Encryption of a triple-length DES key using the ANSI X9.17 method.Used for encrypting keys for local use (under a Variant LMK) or for importing/exporting keys (e.g. under a ZMK). The use of this scheme requires some security settings to be changed. |
| V | Encryption of double/triple-length DES keys using Verifone/GISKE methods. Only used for exporting keys (e.g. under a ZMK). |
| R |Encryption of single/double/triple-length DES keys using the X9 TR-31 Key Block methods.Only used for importing/exporting keys (e.g. under a ZMK). The use of this scheme requires optional license HSM9-LIC006. |
| S |Encryption of all DES, AES, RSA & HMAC keys using Thales Key Block methods. Used for encrypting keys for local use (under a Key Block LMK) or for importing/exporting keys (e.g. under a ZMK) |