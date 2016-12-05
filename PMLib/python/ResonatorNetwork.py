from ResonatorBase import ResonatorBase
from Resonator1D import Resonator1D
from Resonator2D import Resonator2D
from sparse_add import spdistr1D,spdistr2D
from scipy.sparse import lil_matrix,identity,hstack,vstack,csc_matrix
from scipy.linalg import solve
from scipy.linalg import eig
from math import pi
import scipy as sp
import numpy as np
import warnings,os
from numbers import Number
import copy

class ResonatorNetwork():
    SR = 44100
    k = 1./SR

    def __init__(self,objs=None,connPointMatrix=None,massMatrix=None,excPointMatrix=None,readoutPointMatrix=None):
        if objs is None:
            self._objs = [Resonator1D(),Resonator1D(101)]
        else:
            self._objs = objs
        if connPointMatrix is None:
            self._connPointMatrix = [[0.5],[0.5]]
        else:
            self._connPointMatrix = connPointMatrix
        if massMatrix is None:
            self._massMatrix = [[1.],[1.]]
        else:
            self._massMatrix = massMatrix
        if excPointMatrix is None:
            self._excPointMatrix = [[0.5],[0.]]
        else:
            self._excPointMatrix = excPointMatrix
        if readoutPointMatrix is None:
            self.readoutPointMatrix = [[0.4],[0.]]
        else:
            self.readoutPointMatrix = readoutPointMatrix

    @property
    def objs(self):
        return self._objs

    @property
    def connPointMatrix(self):
        return self._connPointMatrix

    @property
    def massMatrix(self):
        return self._massMatrix

    @property
    def excPointMatrix(self):
        return self._excPointMatrix

    @property
    def readoutPointMatrix(self):
        return self._readoutPointMatrix

    @objs.setter
    def objs(self,newObjs):
        try:
            [obj.k for obj in newObjs]
        except AttributeError:
            raise AttributeError('argument objs contains an invalid object')
        else:
            self._objs = newObjs[:]

    @connPointMatrix.setter
    def connPointMatrix(self,newConnPointMatrix):
        try:
            len(newConnPointMatrix)
        except TypeError:
            raise TypeError('argument connPointMatrix must be a 2D indexable object')
        else:
            self.__checkRangeOfArray(newConnPointMatrix,'connPointMatrix')
            self.__checkNrOfElmsInColOfArray(newConnPointMatrix,'connPointMatrix')
            self._connPointMatrix = copy.deepcopy(newConnPointMatrix)

    @massMatrix.setter
    def massMatrix(self,newMassMatrix):
        try:
            len(massMatrix)
        except TypeError:
            raise TypeError('argument massMatrix must be a 2D indexable object')
        else:
            self.__checkNrOfElmsInColOfArray(newMassMatrix,'massMatrix')
            self._massMatrix = copy.deepcopy(newMassMatrix)

    @excPointMatrix.setter
    def excPointMatrix(self,newExcPointMatrix):
        try:
            len(newExcPointMatrix)
        except TypeError:
            raise TypeError('argument excPointMatrix must be a 2D indexable object')
        else:
            self.__checkRangeOfArray(newExcPointMatrix,'excPointMatrix')
            self._excPointMatrix = copy.deepcopy(newExcPointMatrix)

    @readoutPointMatrix.setter
    def readoutPointMatrix(self,newReadoutPointMatrix):
        try:
            len(newReadoutPointMatrix)
        except TypeError:
            raise TypeError('argument readoutPointMatrix must be a 2D indexable object')
        else:
            self.__checkRangeOfArray(newReadoutPointMatrix,'readoutPointMatrix')
            self._readoutPointMatrix = copy.deepcopy(newReadoutPointMatrix)

    # public methods
    def calcModes(self,minFreq=20.,maxFreq=SR/2,minT60=0.01):
        self.__checkDimensions()
        for m in self.objs:
            m.constrUpdateMatrices(); m.constrCouplingMatrices()
        self.Nt = [m.Nm for m in self.objs]          # cumulative is dimension of final block
        A = self.__constrStateTransitionMatrix()
        _lambda,v = eig(A.todense())    # find eigenvalues and eigenvectors
        lambda_abs = np.abs(_lambda)

        # construct input and output matrices and compute diagonalised versions
        self._B = solve(v,self.__constrInputMatrix().todense())
        self._S = self.__constrOutputMatrix()*v

        # filter modes
        wmax = 2.*pi*maxFreq/self.__class__.SR; wmin = 2.*pi*minFreq/self.__class__.SR
        rmin = 1. - 6.91/(minT60*self.__class__.SR)
        lambda_arg = np.arccos(_lambda.real/lambda_abs)
        idx = (_lambda.imag >= 0.) & (lambda_abs < 1.) & (lambda_arg >= wmin) & (lambda_arg <= wmax) & (lambda_abs >= rmin)
        _lambda = _lambda[idx]; lambda_abs = lambda_abs[idx]; lambda_arg = lambda_arg[idx]
        v = v[:,idx]; self._B = self._B[idx,:]; self._S = self._S[:,idx]

        # sort from largest to smallest eigenvalue
        idx = _lambda.argsort()[::-1]
        self.eigenvalues = _lambda[idx]
        self.angle = lambda_arg[idx]; self.radius = lambda_abs[idx]
        self.eigenvectors = v[idx,:];
        self._B = self._B[idx,:]; self._S = self._S[:,idx]

    def calcBiquadCoefs(self,gain=1):
        try:
            lambda_real = self.eigenvalues.real; lambda_imag = self.eigenvalues.imag
            Sr = self._S.real; Si = self._S.imag; Br = self._B.real; Bi = self._B.imag
            a1 = gain*-2.*(np.einsum('in,nj->ijn',-Sr,Br) + np.einsum('in,nj->ijn',Si,Bi))
            a2 = gain*-2.*(np.einsum('in,nj,n->ijn',Sr,Br,lambda_real) - np.einsum('in,nj,n->ijn',Si,Bi,lambda_real) \
            + np.einsum('in,nj,n->ijn',Sr,Bi,lambda_imag) + np.einsum('in,nj,n->ijn',Si,Br,lambda_imag))
            b1 = -2.*lambda_real; b2 = self.radius**2
            self.biquadCoefs = {'a1':a1,'a2':a2,'b1':b1,'b2':b2}
        except AttributeError:
            warnings.warn('eigenvalues are not calculated yet, please execute calcModes first')

    def saveAsJSON(self,path=os.path.dirname(os.path.realpath(__file__)) + '/modalData.json',include='yyyy'):
        try:
            import json
            if '.json' not in path:
                path = str(path) + '.json'
            dict = {}
            for char,i in zip(include,range(4)):
                if char == 'y':
                    if i == 0:
                        coefs = {}
                        for key in self.biquadCoefs:
                            coefs[key] = self.biquadCoefs[key].tolist()
                        dict['biquadCoefs'] = coefs
                    elif i == 1:
                        dict['eigenvaluesPolar'] = { 'radius':self.radius.tolist(),'angle':self.angle.tolist() }
                    elif i == 2:
                        dict['eigenvaluesRect'] = { 'real':self.eigenvalues.real.tolist(),'imag':self.eigenvalues.imag.tolist() }
                    elif i == 3:
                        dict['eigenvectors'] = { 'real':self.eigenvectors.real.tolist(),'imag':self.eigenvectors.imag.tolist() }
            with open(path,'w') as outfile:
                json.dump(dict, outfile)
            return True
        except:
            import sys
            print sys.exc_info()
            return False

    # private methods
    def __checkRangeOfArray(self,mat,arg_name):
        for row in mat:
            for elm in row:
                if isinstance(elm,Number):
                    if elm > 1:
                        raise Exception('argument ' + arg_name + ' may only contain elements between 0 - 1')
                else:
                    if elm[0] > 1 or elm[1] > 1:
                        raise Exception('argument ' + arg_name + ' may only contain elements between 0 - 1')

    def __checkNrOfElmsInColOfArray(self,mat,arg_name):
        #if np.any(np.sum(mat > 0,axis=0) > 2):
        if (any([sum([mat[p][q] > 0 for p in xrange(len(mat))]) for q in xrange(len(mat[0]))]) > 2):
            raise Exception('argument ' + arg_name + ' may only contain 2 nonzero elements per column')

    def __checkDimOfColls(self,coll1,coll2,err_str):
        if isinstance(coll1,np.ndarray) and isinstance(coll2,np.ndarray):
            if coll1.shape[0] != coll2.shape[0]: raise Exception(err_str)
        elif (isinstance(coll1,list) or isinstance(coll1,tuple)) and isinstance(coll2,np.ndarray):
            if len(coll1) != coll2.shape[0]: raise Exception(err_str)
        elif isinstance(coll1,np.ndarray) and (isinstance(coll2,list) or isinstance(coll2,tuple)):
            if coll1.shape[0] != len(coll2): raise Exception(err_str)
        elif (isinstance(coll1,list) or isinstance(coll1,tuple)) and (isinstance(coll2,list) or isinstance(coll2,tuple)):
            if len(coll1) != len(coll2): raise Exception(err_str)

    def __checkDimensions(self):
        self.__checkDimOfColls(self.connPointMatrix,self.excPointMatrix,\
        'arguments connPointMatrix and excPointMatrix must contain an equal nr. of rows')
        self.__checkDimOfColls(self.connPointMatrix,self.objs,\
        'the nr. of rows of argument connPointMatrix must be equal to the nr. of elements in objs')
        self.__checkDimOfColls(self.excPointMatrix,self.objs,\
        'the nr. of rows of argument excPointMatrix must be equal to the nr. of elements in objs')
        self.__checkDimOfColls(self.readoutPointMatrix,self.objs,\
        'the nr. of rows of argument readoutPointMatrix must be equal to the nr. of elements in objs')

    def __iscoll(self,arr):
        if isinstance(arr,list) or isinstance(arr,tuple):
            return True
        else:
            return False

    # maybe not necessary
    def __checkCoordinatesForPlate(self,mat,arg_name):
        if any([[not(point == 0 or isinstance(point,list)) for point in mat[i][:]] for obj,i\
        in zip(self.objs,xrange(len(self.objs))) if isinstance(obj,Resonator2D)][0]):
            raise Exception(arg_name + ' contains one or more invalid coordinates for a plate object')

    def __constrStateTransitionMatrix(self):
        k = self.__class__.k
        A = None    # state transtion block matrix
        # loop over all rows (i.e. individual objs) and find all connections with other objs
        for row,i,obj in zip(self.massMatrix,xrange(len(self.objs)),self.objs):
            fac = 1./(1. + obj.b1*k)
            fac /= obj.h**2 if isinstance(obj,Resonator2D) else obj.h
            C1_total = csc_matrix((obj.Nm,obj.Nm)); C2_total = csc_matrix((obj.Nm,obj.Nm))
            C3_total = {}; C4_total = {}; A_row = None
            colInds = np.nonzero(row)[0]
            # for every connection between obj q and r other objects, construct inter-connection matrices
            for j in colInds:
                cpoint_q = self.connPointMatrix[i][j]
                e_q = spdistr2D(1.,cpoint_q[0],cpoint_q[1],obj.Nx - 1,obj.Ny - 1,flatten=True)\
                if isinstance(obj,Resonator2D) else spdistr1D(1.,cpoint_q,obj.Nm,'lin')
                # return the row indices of the nonzero entrees in the current col we are looking in
                row_r = [ind for ind,item in enumerate([self.massMatrix[q][j] for q in xrange(len(self.massMatrix))]) if item > 0]
                # remove row index of current object and since list must now be of size 1, simply return row index
                row_r.remove(i); row_r = row_r[0]
                M = float(self.massMatrix[i][j])/self.massMatrix[row_r][j]    # mass ratio: Mq/Mr
                cpoint_r = self.connPointMatrix[row_r][j]
                e_r = spdistr2D(1.,cpoint_r[0],cpoint_r[1],self.objs[row_r].Nx - 1,self.objs[row_r].Ny - 1,flatten=True) \
                if isinstance(self.objs[row_r],Resonator2D) \
                else spdistr1D(1.,cpoint_r,self.objs[row_r].B.shape[0],'lin')
                c1 = fac/(e_q.T.dot(e_q)[0,0] + M*e_r.T.dot(e_r)[0,0])
                e_qCre_q = e_q*e_q.T; e_qCre_r = e_q*e_r.T
                C1_total = C1_total + c1*e_qCre_q*obj.C1
                C2_total = C2_total + c1*e_qCre_q*obj.C2
                if row_r in C3_total:   # save to assert that when C3[row_r] is empty, C4[row_r] is empty also
                    C3_total[row_r] = C3_total[row_r] - c1*e_qCre_r*self.objs[row_r].C1
                    C4_total[row_r] = C4_total[row_r] - c1*e_qCre_r*self.objs[row_r].C2
                else:
                    C3_total[row_r] = -c1*e_qCre_r*self.objs[row_r].C1
                    C4_total[row_r] = -c1*e_qCre_r*self.objs[row_r].C2

            # construct row of A for u[n]
            for j in xrange(0,len(self.objs)):
                if i == j:       # we're on the diagonal
                    A_row = hstack((obj.B + C1_total,obj.C + C2_total),format="lil") if A_row == None else \
                    hstack((A_row,obj.B + C1_total,obj.C + C2_total),format="lil")
                elif j in C3_total:
                    A_row = hstack((C3_total[j],C4_total[j]),format="lil") if A_row == None else \
                    hstack((A_row,C3_total[j],C4_total[j]),format="lil")
                else:
                    Nm2 = self.objs[j].Nm*2
                    A_row = lil_matrix((obj.Nm,Nm2)) if A_row is None else \
                    hstack((A_row,lil_matrix((obj.Nm,Nm2))))

            # construct row of A for u[n - 1]
            if i == 0:   # first object, so identity matrix is first in row
                I = hstack((identity(obj.Nm,format="lil"),lil_matrix((obj.Nm,A_row.shape[1] - obj.Nm))))
            elif i == len(self.objs) - 1:   # last object, so identity matrix is penultimate to last col
                I = hstack((lil_matrix((obj.Nm,A_row.shape[1] - 2*self.objs[-1].Nm)),\
                identity(obj.Nm,format="lil"),lil_matrix((obj.Nm,obj.Nm))))
            else:   # if any other object, calc pos of identity matrix based on grid size N of each obj
                I = hstack((lil_matrix((obj.Nm,2*np.sum(self.Nt[:i]))),identity(obj.Nm),\
                lil_matrix((obj.Nm,obj.Nm + 2*np.sum(self.Nt[-(len(self.Nt) - 1 - i):])))))
            # append row to block state transition matrix A
            A = vstack((A_row,I)) if A is None else vstack((A,A_row,I))

        return A.tocsc()

    def __constrInputMatrix(self):
        B = None; k = self.__class__.k
        for row,obj in zip(self.excPointMatrix,self.objs):
            E = lil_matrix((obj.Nm*2,len(self.excPointMatrix[0])))
            for ep,i in zip(row,xrange(0,len(row))):
                if ep > 0.0:
                    E[:obj.Nm,i] = spdistr2D(k**2/((1. + obj.b1*k)*obj.h**2),ep[0],ep[1],obj.Nx - 1,obj.Ny - 1,flatten=True)\
                    if isinstance(obj,Resonator2D) else spdistr1D(k**2/((1. + obj.b1*k)*obj.h),ep,obj.Nm,'lin')
            B = E if B is None else vstack((B,E))

        return B.tocsc()

    def __constrOutputMatrix(self):
        S = None; k = self.__class__.k
        for col in [list(x) for x in zip(*self.readoutPointMatrix)]:    # transpose self.readoutPointMatrix
            E = None
            for rp,obj in zip(col,self.objs):
                if rp > 0.0:
                    e = spdistr2D(1./k,rp[0],rp[1],obj.Nx - 1,obj.Ny - 1,flatten=True).T\
                    if isinstance(obj,Resonator2D) else spdistr1D(1./k,rp,obj.Nm,'lin').T
                    E = hstack((e,-e)) if E is None else hstack((E,e,-e))
                else:
                    e = lil_matrix((1,obj.Nm*2))
                    E = e if E is None else hstack((E,e))
            S = E if S is None else vstack((S,E))

        return S.tocsc()
