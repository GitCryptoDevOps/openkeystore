function deserializeTest (x509_spki, jcs_pk)
{
    var x509_spki_bin = org.webpki.util.Base64URL.decode (x509_spki);
    var seq = new org.webpki.crypto.createPublicKeyFromSPKI (x509_spki_bin);
    console.debug ("Key type : " + (seq.rsa_flag ? "RSA" : "EC"));
    var reader = org.webpki.json.JSONParser.parse (jcs_pk);
    if (!org.webpki.util.ByteArray.equals (x509_spki_bin, reader.getPublicKey ()))
    {
        throw "Didn't match: ";
    }
}

var p256_key =
'{\
  "PublicKey": \
    {\
      "EC": \
        {\
          "NamedCurve": "http://xmlns.webpki.org/sks/algorithm#ec.nist.p256",\
          "X": "GRgbhKB9Mw1lDKJFMbD_HsBvHR9235X7zF2SxHkDiOU",\
          "Y": "isxpqxSx6AAEmZfgL5HevS67ejfm_4HcsB883TUaccs"\
        }\
    }\
}';

var p256_key_xml =
'{\
    "PublicKey": \
      {\
        "EC": \
          {\
            "NamedCurve": "urn:oid:1.2.840.10045.3.1.7",\
            "X": "GRgbhKB9Mw1lDKJFMbD_HsBvHR9235X7zF2SxHkDiOU",\
            "Y": "isxpqxSx6AAEmZfgL5HevS67ejfm_4HcsB883TUaccs"\
          }\
      }\
  }';

var p256_key_spki = 'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEGRgbhKB9Mw1lDKJFMbD_HsBvHR9235X7zF2SxHkDiOWKzGmrFLHoAASZl-Avkd69Lrt6N-b_gdywHzzdNRpxyw';

var rsa_2048_key = 
'{\
    "@context": "http://keys/test",\
    "PublicKey": \
      {\
        "RSA": \
          {\
            "Modulus": "6mct2A1crFheV3fiMvXzwFJgR6fWnBRyg6X0P_uTQOlll1orTqd6a0QTTjnm1XlM5XF8g5SyqhIO4kLUmvJvwEHaXHHkbn\
8N4gHzhbPA7FHVdCt37W5jduUVWHlBVoXIbGaLrCUj4BCDmXImhOHxbhRvyiY2XWcDFAGt_60IzLAnPUof2Rv-aPNYJY6qa0yvnJmQp4yNPsIpHYpj9Sa3\
rctEC2OELZy-HTlDBVyzEYwnmDXtvhjoPEaUZUyHaJTC_LZMOTsgJqDT8mOvHyZpLH_f7u55mXDBoXF0iG9sikiRVndkJ18wZmNRow2UmK3QB6G2kUYxt3\
ltPOjDgADLKw",\
            "Exponent": "AQAB"\
          }\
      }\
  }';

var rsa_2048_key_spki = 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6mct2A1crFheV3fiMvX\
zwFJgR6fWnBRyg6X0P_uTQOlll1orTqd6a0QTTjnm1XlM5XF8g5SyqhIO4kLUmvJvwEHaXHHkbn8N4gHzhbPA7FHVdCt37W5jduUVWHlBVoXIbGaLrCUj4\
BCDmXImhOHxbhRvyiY2XWcDFAGt_60IzLAnPUof2Rv-aPNYJY6qa0yvnJmQp4yNPsIpHYpj9Sa3rctEC2OELZy-HTlDBVyzEYwnmDXtvhjoPEaUZUyHaJT\
C_LZMOTsgJqDT8mOvHyZpLH_f7u55mXDBoXF0iG9sikiRVndkJ18wZmNRow2UmK3QB6G2kUYxt3ltPOjDgADLKwIDAQAB';

var p521_key =
'{\
    "PublicKey": \
      {\
        "EC": \
          {\
            "NamedCurve": "http://xmlns.webpki.org/sks/algorithm#ec.nist.p521",\
            "X": "AQggHPZ-De2Tq_7U7v8ADpjyouKk6eV97Lujt9NdIcZgWI_cyOLv9HZulGWtC7I3X73ABE-rx95hAKbxiqQ1q0bA",\
            "Y": "_nJhyQ20ca7Nn0Zvyiq54FfCAblGK7kuduFBTPkxv9eOjiaeGp7V_f3qV1kxS_Il2LY7Tc5l2GSlW_-SzYKxgek"\
          }\
      }\
  }';

var p521_key_spki = 'MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBCCAc9n4N7ZOr_tTu\
_wAOmPKi4qTp5X3su6O3010hxmBYj9zI4u_0dm6UZa0LsjdfvcAET6vH3mEApvGKpDWrRsAA_nJhyQ20ca7Nn0Zvyiq54FfCAblGK7kuduF\
BTPkxv9eOjiaeGp7V_f3qV1kxS_Il2LY7Tc5l2GSlW_-SzYKxgek';

var b283_key = 
'{\
    "PublicKey": \
      {\
        "EC": \
          {\
            "NamedCurve": "http://xmlns.webpki.org/sks/algorithm#ec.nist.b283",\
            "X": "A0QgZzqf_IMeC-sOCBEOZhGmGHD0luoasQAK4-AVYtk0u2bD",\
            "Y": "BDdnv7LEFj3pN18G8NfTdf6nW171eWS6DLPjstH4i-wSgehw"\
          }\
      }\
  }';

var b283_key_spki = 'MF4wEAYHKoZIzj0CAQYFK4EEABEDSgAEA0QgZzqf_IMeC-sOCBEOZhGm\
GHD0luoasQAK4-AVYtk0u2bDBDdnv7LEFj3pN18G8NfTdf6nW171eWS6DLPjstH4i-wSgehw';

var p192_key = 
'{\
    "PublicKey": \
      {\
        "EC": \
          {\
            "NamedCurve": "http://xmlns.webpki.org/sks/algorithm#ec.nist.p192",\
            "X": "QWOcZWv7yoeLxbxtA6CHZoSdpmlg_u69",\
            "Y": "QtAQygGMLDy4Lp2MLtTYQlyQZGUfCQQv"\
          }\
      }\
  }';

var p192_key_spki = 'MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEQWOcZWv7yoeLxbxtA6CHZ\
oSdpmlg_u69QtAQygGMLDy4Lp2MLtTYQlyQZGUfCQQv';

var p384_key = 
'{\
    "PublicKey": \
      {\
        "EC": \
          {\
            "NamedCurve": "http://xmlns.webpki.org/sks/algorithm#ec.nist.p384",\
            "X": "MyQMdQM9i47obgf_KDINLfjPaa03y8S_dDenvY5ULGOmoVlki6cvGRpL0QCiw_XD",\
            "Y": "JwLihlcNyevQvl30kqwVlyHWNSZ1z1LGO4VyxmrMdb8R2egVzMakm4PtPJjf5gMX"\
          }\
      }\
  }';

var p384_key_spki = 'MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEMyQMdQM9i47obgf_KDINLf\
jPaa03y8S_dDenvY5ULGOmoVlki6cvGRpL0QCiw_XDJwLihlcNyevQvl30kqwVlyHWNSZ1z1LGO4VyxmrMdb8R2egVzMakm4PtPJjf5gMX';

var brainpool256_key =
'{\
    "PublicKey": \
      {\
        "EC": \
          {\
            "NamedCurve": "http://xmlns.webpki.org/sks/algorithm#ec.brainpool.p256r1",\
            "X": "AZ8WB15YNakVM9TeblaZh2HmmO2lDTarnXROAh7LO0Q",\
            "Y": "lal3Vzb5AjElCdazXnpCaa2gdU2LrMucG51oRHXOoHM"\
          }\
      }\
  }';

var brainpool256_key_spki = 'MFowFAYHKoZIzj0CAQYJKyQDAwIIAQEHA0IABAGfFgdeWDWpF\
TPU3m5WmYdh5pjtpQ02q510TgIeyztElal3Vzb5AjElCdazXnpCaa2gdU2LrMucG51oRHXOoHM';

var b163_key = 
'{\
    "PublicKey": \
      {\
        "EC": \
          {\
            "NamedCurve": "http://xmlns.webpki.org/sks/algorithm#ec.nist.b163",\
            "X": "B0M_ADx-Ma7KuLYX1kiHkeA5be9Z",\
            "Y": "YrAnUJj08HWJ3wnTfpWzy-S0t-c"\
          }\
      }\
  }';

var b163_key_spki = 'MEAwEAYHKoZIzj0CAQYFK4EEAA8DLAAEB0M_ADx-Ma7KuLYX1kiHkeA5\
be9ZAGKwJ1CY9PB1id8J036Vs8vktLfn';

var b233_key =
'{\
    "PublicKey": \
      {\
        "EC": \
          {\
            "NamedCurve": "http://xmlns.webpki.org/sks/algorithm#ec.nist.b233",\
            "X": "_b9j6YxMzS-qk6p0dY_WCf5_04gyFaVwdHn6PGg",\
            "Y": "kotlfOdTNbKK5Z8co1-Ykh22rpMorG0llNrJe2Q"\
          }\
      }\
  }';

var b233_key_spki = 'MFIwEAYHKoZIzj0CAQYFK4EEABsDPgAEAP2_Y-mMTM0vqpOqdHWP1gn-f9OIMhW\
lcHR5-jxoAJKLZXznUzWyiuWfHKNfmJIdtq6TKKxtJZTayXtk';

deserializeTest (b163_key_spki, b163_key);

deserializeTest (b233_key_spki, b233_key);

deserializeTest (b283_key_spki, b283_key);

deserializeTest (p192_key_spki, p192_key);

deserializeTest (p256_key_spki, p256_key);
deserializeTest (p256_key_spki, p256_key_xml);

deserializeTest (p384_key_spki, p384_key);

deserializeTest (p521_key_spki, p521_key);

deserializeTest (brainpool256_key_spki, brainpool256_key);

deserializeTest (rsa_2048_key_spki, rsa_2048_key);

console.debug ("Key serializing tests successful!");


