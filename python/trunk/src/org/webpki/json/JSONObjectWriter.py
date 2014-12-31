from collections import OrderedDict

from Crypto.PublicKey import RSA
from Crypto.Signature import PKCS1_v1_5

from ecdsa.util import sigencode_der

from org.webpki.json import JCSSignatureKey

from org.webpki.json.Utils import base64UrlEncode
from org.webpki.json.Utils import getAlgorithmEntry
from org.webpki.json.Utils import serializeJson

class new:
  def __init__(self,optionalRoot=None):
    if optionalRoot:
      self.root = optionalRoot
    else:
      self.root = OrderedDict()

  def setInt(self,name,value):
    if not isinstance(value,int):
      raise TypeError('Integer expected')
    return self.put(name,value)

  def setString(self,name,value):
    if not isinstance(value,str):
      raise TypeError('String expected')
    return self.put(name,value)

  def setFloat(self,name,value):
    if isinstance(value, int):
      value = float(value)
    elif not isinstance(value,float):
      raise TypeError('Float expected')
    return self.put(name,value)

  def setObject(self,name, optionalRoot=None):
    newObject = new(optionalRoot)
    self.put(name,newObject.root)
    return newObject

  def setBinary(self,name,value):
    if not isinstance(value, str):
      raise TypeError('String or bytearray expected')
    return self.put(name,base64UrlEncode(value))

  def setSignature(self,signatureKey):
    if not isinstance(signatureKey,JCSSignatureKey.new):
      raise TypeError('JCSSignature expected')
    signatureObject = new()
    signatureObject.setString('algorithm',signatureKey.algorithm)
    signatureObject.setObject('publicKey',signatureKey.getPublicKeyParameters())
    self.put('signature',signatureObject.root)
    algorithmEntry = getAlgorithmEntry(signatureKey.algorithm)
    hashObject = algorithmEntry[1].new(self.serialize().encode("utf-8"))
    if signatureKey.isRSA():
      signer = PKCS1_v1_5.new(signatureKey.nativePrivateKey)
      signatureObject.setBinary('value',signer.sign(hashObject))
    else:
      signatureObject.setBinary('value',signatureKey.nativePrivateKey.sign_digest(hashObject.digest(),sigencode=sigencode_der))
    return self

  def put(self,name,value):
    if not isinstance(name,str):
      raise TypeError('Name must be a string')
    if name in self.root:
      raise ValueError('Duplicate property: "' + name + '"')
    self.root[name] = value
    return self

  def serialize(self):
    return serializeJson(self.root)
