class ResonatorBase(object):
  SR = 44100
  k = 1./SR

  def __init__(self,gamma=200,kappa=1,b1=0,b2=0,boundaryCond=None):
    self._gamma = gamma
    self._kappa = kappa
    self._b1 = b1
    self._b2 = b2
    self._boundaryCond = boundaryCond

  @property
  def gamma(self):
    """spatially scaled wave speed of the string"""
    return self._gamma

  @property
  def kappa(self):
    """spatially scaled stiffness coefficient"""
    return self._kappa

  @property
  def b1(self):
    """frequency independent damping constant of the string"""
    return self._b1

  @property
  def b2(self):
    """frequency dependent damping constant of the string"""
    return self._b2

  @property
  def boundaryCond(self):
    """boundary condition of the object"""
    return self._boundaryCond

  @gamma.setter
  def gamma(self,newGamma):
    if 0 <= newGamma <= self.__class__.SR/2:
      self._gamma = newGamma
    else:
      raise ValueError('argument gamma has to be a real number between 0 - %' % self.__class__.SR/2)

  @kappa.setter
  def kappa(self,newKappa):
    if newKappa >= 0:
      self._kappa = newKappa
    else:
      raise ValueError('argument inharmCoef must be a real number greater than or equal to 0')

  @b1.setter
  def b1(self,newB1):
    if newB1 >= 0:
      self._b1 = newB1
    else:
      raise ValueError('argument b1 must be a real number greater than or equal to 0')

  @b2.setter
  def b2(self,newB2):
    if newB2 >= 0:
      self._b2 = newB2
    else:
      raise ValueError('argument b2 must be a real number greater than or equal to 0')

  @boundaryCond.setter
  def boundaryCond(self,newBoundaryCond):
    if newBoundaryCond in self.__class__.validBoundaryConds:
      self._boundaryCond = newBoundaryCond
    else:
      raise Exception('argument boundaryCond does not represent a valid boundary condition')
