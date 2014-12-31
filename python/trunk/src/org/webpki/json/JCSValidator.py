from collections import OrderedDict

from Crypto.PublicKey import RSA
from Crypto.Signature import PKCS1_v1_5

from ecdsa.util import sigdecode_der
from ecdsa import VerifyingKey

from org.webpki.json.Utils import cryptoBigNumDecode
from org.webpki.json.Utils import base64UrlDecode
from org.webpki.json.Utils import listKeys
from org.webpki.json.Utils import getEcCurve
from org.webpki.json.Utils import serializeJson
from org.webpki.json.Utils import getAlgorithmEntry

############################################
# JCS (JSON Cleartext Signature) validator #
############################################

class new:
  def __init__(self,jsonObject):
    if not isinstance(jsonObject, OrderedDict):
      raise TypeError('JCS requires JSON to be parsed into a "OrderedDict"')
    signatureObject = jsonObject['signature']
    clonedSignatureObject = OrderedDict(signatureObject)
    signatureValue = base64UrlDecode(signatureObject.pop('value'))
    algorithmEntry = getAlgorithmEntry(signatureObject['algorithm'])
    hashObject = algorithmEntry[1].new(serializeJson(jsonObject).encode("utf-8"))
    jsonObject['signature'] = clonedSignatureObject
    self.publicKey = signatureObject['publicKey']
    self.keyType = self.publicKey['type']
    if algorithmEntry[0]:
      if self.keyType != 'RSA':
        raise TypeError('"RSA" expected')
      self.nativePublicKey = RSA.construct([cryptoBigNumDecode(self.publicKey['n']),
                                            cryptoBigNumDecode(self.publicKey['e'])])
      if not PKCS1_v1_5.new(self.nativePublicKey).verify(hashObject,signatureValue):
        raise ValueError('Invalid Signature!')
    else:
      if self.keyType != 'EC':
        raise TypeError('"EC" expected')
      self.nativePublicKey = VerifyingKey.from_string(base64UrlDecode(self.publicKey['x']) + 
                                                      base64UrlDecode(self.publicKey['y']),
                                                      curve=getEcCurve(self.publicKey['curve']))
      self.nativePublicKey.verify_digest(signatureValue,hashObject.digest(),sigdecode=sigdecode_der)
      
  def getPublicKey(self,type='PEM'):
    if type == 'PEM':
      if self.keyType == 'RSA':
        return self.nativePublicKey.exportKey(format='PEM') + '\n'
      else:
        return self.nativePublicKey.to_pem()
    elif type == 'Native':
      return self.nativePublicKey
    elif type == 'JWK':
      jwk = OrderedDict()
      for item in self.publicKey:
        key = item
        if key == 'type':
          key = 'kty'
        jwk[key] = self.publicKey[item]
      return serializeJson(jwk)
    elif type == 'JCS':
      return serializeJson(self.publicKey)
    else:
      raise ValueError('Unknown key type: "' + type + '"') 

# TODO: "extensions", "version", "keyId" and checks for extranous properties

