from ResonatorBase import ResonatorBase
from sparse_diff_matr_2d import laplacian_matrix_2d,biharmonic_matrix_2d
from math import sqrt
import numpy as np
from scipy.sparse import dia_matrix,identity

class Resonator2D(ResonatorBase):
    def __init__(self,gamma=200,kappa=1,b1=0,b2=0,boundaryCond='AllSidesSimplySupported',epsilon=1):
        ResonatorBase.__init__(self,gamma,kappa,b1,b2,boundaryCond)
        self._epsilon = epsilon

    @property
    def epsilon(self):
        """domain aspect ratio: epsilon=Lx/Ly"""
        return self._epsilon

    @epsilon.setter
    def epsilon(self,newEpsilon):
        if newEpsilon > 0:
            self._epsilon = newEpsilon
        else:
            raise ValueError('argument epsilon has to be a real number greater than 0')

    # public methods
    def constrUpdateMatrices(self):
        self.__calcGridStep()       # calculate grid step
        k = self.__class__.k; _lambda = self.gamma*k/self.h; mu = self.kappa*k/self.h**2
        zeta = 2.*self.b2*k/self.h**2; den = 1. + self.b1*k; Nx = self.Nx; Ny = self.Ny
        self.Nm = (self.Nx - 1)*(self.Ny - 1)

        # create update matrices in sparse diagonal form
        Dlapl = laplacian_matrix_2d(Nx,Ny,self.boundaryCond)
        I = identity(self.Nm)

        self.B = (2.*I - mu**2*biharmonic_matrix_2d(Nx,Ny,self.boundaryCond) + (_lambda**2 + zeta)*Dlapl)/den
        self.C = -((1. - self.b1*k)*I + zeta*Dlapl)/den

    def constrCouplingMatrices(self):
        k = self.__class__.k; h = self.h; a = 2.*self.b2*k/(h**2); Nx = self.Nx; Ny = self.Ny
        Dlapl = laplacian_matrix_2d(Nx,Ny,self.boundaryCond)
        self.C1 = ((self.gamma*k/h)**2 + a)*Dlapl - (self.kappa*k/(h**2))**2*biharmonic_matrix_2d(Nx,Ny,self.boundaryCond)
        self.C2 = -a*Dlapl

    # private methods
    def __calcGridStep(self):
        k = self.__class__.k; a = self.gamma**2*k**2 + 4.*self.b2*k
        self.h = sqrt(a + sqrt(a**2 + 16.*self.kappa**2*k**2))
        self.Nx = int(sqrt(self.epsilon)/self.h)
        self.Ny = int(1./(sqrt(self.epsilon)*self.h))
        self.h = sqrt(self.epsilon)/self.Nx
