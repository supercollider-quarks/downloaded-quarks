from ResonatorBase import ResonatorBase
from sparse_diff_matr_1d import second_difference_matrix,fourth_difference_matrix
from math import sqrt
from scipy.sparse import identity

class Resonator1D(ResonatorBase):
    def __init__(self,gamma=200,kappa=1,b1=0,b2=0,boundaryCond='BothSimplySupported'):
        ResonatorBase.__init__(self,gamma,kappa,b1,b2,boundaryCond)

    # public methods
    def constrUpdateMatrices(self):
        self.__calcGridStep()       # calculate grid step
        k = self.__class__.k; _lambda2 = (self.gamma*k/self.h)**2; mu2 = (self.kappa*k/self.h**2)**2
        zeta = 2.*self.b2*k/self.h**2; den = 1. + self.b1*k; N = self.N

        Dxx = second_difference_matrix(N,self.boundaryCond)
        self.Nm = Dxx.shape[0]
        I = identity(self.Nm)

        if 'Free' not in self.boundaryCond:
            self.B = (2.*I + (_lambda2 + zeta)*Dxx - mu2*fourth_difference_matrix(N,self.boundaryCond))/den
            self.C = -((1. - self.b1*k)*I + zeta*Dxx)/den
        else:
            Dxxxx = fourth_difference_matrix(N,self.boundaryCond,{'a0':1. + _lambda2 + zeta,'a1':-(2 + _lambda2 + zeta),'a2':-zeta})
            self.B = (2.*I + (_lambda2 + zeta)*Dxx - mu2*Dxxxx[0])/den
            self.C = -((1. - self.b1*k)*I + zeta*Dxx - mu2*Dxxxx[1])/den

    def constrCouplingMatrices(self):
        k = self.__class__.k; h = self.h; N = self.N; a = 2.*self.b2*k/(h**2)
        _lambda2 = (self.gamma*k/self.h)**2; zeta = 2.*self.b2*k/self.h**2; mu2 = (self.kappa*k/(h**2))**2
        Dxx = second_difference_matrix(N,self.boundaryCond)
        if 'Free' not in self.boundaryCond:
            self.C1 = (_lambda2 + zeta)*Dxx - mu2*fourth_difference_matrix(N,self.boundaryCond)
            self.C2 = -zeta*Dxx
        else:
            Dxxxx = fourth_difference_matrix(N,self.boundaryCond,{'a0':1. + _lambda2 + zeta,'a1':-2. - _lambda2 - zeta,'a2':-zeta})
            self.C1 = (_lambda2 + zeta)*Dxx - mu2*Dxxxx[0]
            self.C2 = -zeta*Dxx - mu2*Dxxxx[1]        

    # private methods
    def __calcGridStep(self):
        k = self.__class__.k; a = self.gamma**2*k**2 + 4.*self.b2*k
        self.h = sqrt(0.5*(a + sqrt(a**2 + 16.*self.kappa**2*k**2)))
        self.N = int(1./self.h)
        self.h = 1./self.N
