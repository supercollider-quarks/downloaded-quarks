import numpy as np
import scipy.sparse as sp
from math import floor,sqrt
from warnings import warn

def setdiag_range(mat,arr,ind=(),k=0):
  """
  Similar to instance method setdiag but with the option to specify a specific range along
  the diagonal. Currently only works with matrices of type lil_matrix.
  """
  if not sp.isspmatrix_lil(mat):
    raise ValueError('argument mat must be of type scipy.sparse.lil_matrix')
  if not isinstance(arr,(list,tuple)):
    raise ValueError('argument arr must be of type list or tuple')
  if not isinstance(ind,(list,tuple)):
    raise ValueError('argument ind must be of type list or tuple')

  if ind[0] == 0:
    mat.setdiag(arr[0:ind[1]],k)
  diag_size = min(mat.shape) - abs(k); do_fill = False
  if ind[1] < 0 and ind[0] < ind[1] and diag_size + ind[1] > 0:
    start_ind = max(diag_size + ind[0],0); end_ind = diag_size + ind[1]
    do_fill = True
  elif ind[0] > 0 and ind[1] >= ind[0] and ind[0] < diag_size:
    start_ind = ind[0]; end_ind = min(ind[1],diag_size - 1)
    do_fill = True
  if do_fill:
    j = 0
    if k == 0:
      for i in xrange(start_ind,end_ind + 1):
        if j < len(arr): mat[i,i] = arr[j]; j += 1
        else: break
    elif k > 0:
      for i in xrange(start_ind,end_ind + 1):
        if j < len(arr): mat[i,i + k] = arr[j]; j += 1
        else: break

def sptoeplitz(col,*args):
  """
  SPTOEPLITZ(COL,ROW) produces a sparse nonsymmetric Toeplitz matrix having COL as its first
  column and ROW as its first row. Neither Col nor Row needs to be sparse. No full-size dense
	matrices are formed.

	SPTOEPLITZ(ROW) is a sparse symmetric/Hermitian Toeplitz matrix.

	Examples:
	>>> import numpy as np
	>>> from sparse_add import sptoeplitz
	>>> sptoeplitz(np.real(1j**np.linspace(0,8,9)))                        # 9x9, 41 nonzeros
	>>> sptoeplitz(np.concatenate((np.array([-2,1]),np.array([0]*9998))))  # classic 2nd difference

	See also TOEPLITZ, SPDIAGS

	Copyright (c) 2006 by Tobin Driscoll (tobin.driscoll@gmail.com).
	First version, 11 December 2006.

	Adapted to Python by Michael Dzjaparidze 2012.
	"""
  if isinstance(col,str): col = eval(col)
  try:
    col[0]; col = np.asarray(col)
  except TypeError:
    col = np.array([col])
  except IndexError:
    raise IndexError('argument col is an empty collection');

  if len(args) == 0:
    col[0] = col[0].conjugate(); row = col.copy(); col = np.conjugate(col)
  else:
    row = eval(args[0]) if isinstance(args[0],str) else args[0]
    try:
      row[0]; row = np.asarray(row)
    except TypeError:
      row = np.array([row])
    except IndexError:
      raise IndexError('argument row is an empty collection')
    if col[0] != row[0]:
      warn('first element of input column does not match first'\
      'element of input row, column wins diagonal conflict')

  m = len(col); n = len(row)

  # locate indices of nonzero elements
  ic = np.nonzero(col); sc = col[ic]
  row[0] = 0; ir = np.nonzero(row); sr = row[ir]

  d = np.concatenate((ir[0],0 - ic[0]))
  B = np.transpose(np.tile(np.concatenate((sr,sc)),(np.minimum(m,n),1)))
  return sp.spdiags(B,d,m,n)

def spdistr(ymax,x0,N,p='trunc',eps=1e-09):
  try:
    ymax = float(ymax)
  except (ValueError,TypeError):
    raise Exception('argument ymax has to be a real number')
  try:
    x0[0]; [float(elm) for elm in x0]
    if any([not 0 <= elm <= 1 for elm in x0]): raise ValueError
    if not p in ('trunc','bilin'):
      raise Exception('argument p has to be either "trunc" or "bilin"')
    dim = True
  except (IndexError,TypeError,ValueError):
    try:
      x0 = float(x0)
      if not 0 <= x0 <= 1: raise ValueError
      if not p in ('trunc','lin','cubic'):
        raise Exception('argument p has to be either "trunc", "lin" or "cubic"')
      dim = False
    except (ValueError,TypeError):
      raise Exception('argument x0 has to be a real number between 0 - 1')
  try:
    N[0]; [int(elm) for elm in N[0:-1]]
    if any([not elm > 3 for elm in N[0:-1]]): raise ValueError
    float(N[-1])
    if not N[-1] > 0:
      raise Exception('3rd element in N - epsilon - has to be a positive real number')
  except (IndexError,TypeError,ValueError):
    try:
      N = int(N)
      if not N > 3: raise ValueError
    except (ValueError,TypeError):
      raise Exception('argument N has to be a positive integer greater than 3')

  if not dim:
    # derived parameters
    h = 1./N                 # grid size
    l0 = int(floor(x0/h))    # integer observation index
    alpha0 = x0/h - l0       # fractional remainder after truncation

    E = sp.lil_matrix((N+1,1))

    if p == 'trunc' or alpha0 < eps:
      E[l0,0] = ymax
    elif p == 'lin' or (p == 'cubic' and (x0 < h or 1.-x0 < h)):
      E[l0,0] = (1 - alpha0)*ymax; E[l0+1,0] = alpha0*ymax
    else:
      E[l0-1,0] = -(1./6)*alpha0*(alpha0 - 1)*(alpha0 - 2)*ymax
      E[l0,0] = 0.5*(alpha0 - 1)*(alpha0 + 1)*(alpha0 - 2)*ymax
      E[l0+1,0] = -0.5*alpha0*(alpha0 + 1)*(alpha0 - 2)*ymax
      E[l0+2,0] = (1./6)*alpha0*(alpha0 + 1)*(alpha0 - 1)*ymax

    return E.tocsc()
  else:
    # derived parameters
    h = sqrt(N[2])/N[0]                      # grid size
    l0 = int(floor(sqrt(N[2])*x0[0]/h))      # integer horizontal observation index
    m0 = int(floor(x0[1]/(sqrt(N[2])*h)))    # integer vertical observation index
    alphax0 = x0[0]/h - l0           # fractional remainder after truncation in horizontal direction
    alphay0 = x0[1]/h - m0           # fractional remainder after truncation in vertical direction

    E = sp.lil_matrix((N[0] + 1,N[1] + 1))

    if p == 'trunc' or (alphax0 < eps and alphay0 < eps):
      E[l0,m0] = ymax
    else:
      E[l0,m0] = (1 - alphax0)*(1 - alphay0)*ymax
      E[l0,m0+1] = (1 - alphax0)*alphay0*ymax
      E[l0+1,m0] = alphax0*(1 - alphay0)*ymax
      E[l0+1,m0+1] = alphax0*alphay0*ymax

    return E.tocsc()

def checkInputArgs4spdistr1D(ymax,x0,N,p,eps):
  try:
    ymax = float(ymax)
  except (ValueError,TypeError):
    raise Exception('argument ymax has to be a real number')
  try:
    x0 = float(x0)
    if not 0 <= x0 <= 1: raise ValueError
  except (ValueError,TypeError):
    raise Exception('argument x0 has to be a real number between 0 - 1')
  try:
    N = int(N)
    if not N > 3: raise ValueError
  except (ValueError,TypeError):
    raise Exception('argument N has to be a positive integer greater than 3')
  if not p in ('trunc','lin','cubic'):
    raise Exception('argument p has to be either "trunc", "lin" or "cubic"')
  try:
    eps = float(eps)
    if not eps > 0: raise ValueError
  except (ValueError,TypeError):
    raise Exception('argument eps has to be a real number greater than 0')

def spdistr1D_diag(ymax,x0,N,p='trunc',eps=1e-09):
  checkInputArgs4spdistr1D(ymax,x0,N,p,eps)

  # derived parameters

  # grid size
  h = 1./N
  # integer observation index
  l0 = int(floor(x0/h))
  # fractional remainder after truncation
  alpha0 = x0/h - l0

  E = sp.lil_matrix((N + 1,N + 1))

  if p == 'trunc' or alpha0 < eps:
    E[l0,l0] = ymax
  elif p == 'lin' or (p == 'cubic' and (x0 < h or 1.-x0 < h)):
    E[l0,l0] = (1 - alpha0)*ymax; E[l0 + 1,l0 + 1] = alpha0*ymax
  else:
    E[l0-1,l0-1] = -(1./6)*alpha0*(alpha0 - 1)*(alpha0 - 2)*ymax
    E[l0,l0] = 0.5*(alpha0 - 1)*(alpha0 + 1)*(alpha0 - 2)*ymax
    E[l0+1,l0+1] = -0.5*alpha0*(alpha0 + 1)*(alpha0 - 2)*ymax
    E[l0+2,l0+2] = (1./6)*alpha0*(alpha0 + 1)*(alpha0 - 1)*ymax

  return E.tocsr()

def spdistr1D(ymax,x0,N,p='trunc',eps=1e-09):
  """
  SPDISTR(YMAX,X0,N,P) produces a sparse vector of size N+1 representing a P'th order distribution
  centered at X0, which when integrated sums up to YMAX.

  SPDISTR(YMAX,X0,N) produces a sparse vector of size N+1 representing a zero'th order distribution
  centered at X0, which when integrated sums up to YMAX.
  """
  checkInputArgs4spdistr1D(ymax,x0,N,p,eps)

  # derived parameters
  h = 1./N
  l0 = int(floor(x0/h))
  alpha0 = x0/h - l0

  E = sp.lil_matrix((N,1))

  if p == 'trunc' or alpha0 < eps:
    l0 = min(l0,N - 1)
    E[l0,0] = ymax
  elif p == 'lin' or (p == 'cubic' and (x0 < h or 1. - x0 < h)):
    l0 = min(l0,N - 2)
    E[l0,0] = (1 - alpha0)*ymax; E[l0 + 1,0] = alpha0*ymax
  else:
    l0 = min(l0,N - 3)
    E[l0-1,0] = -(1./6)*alpha0*(alpha0 - 1.)*(alpha0 - 2.)*ymax
    E[l0,0] = 0.5*(alpha0 - 1.)*(alpha0 + 1.)*(alpha0 - 2.)*ymax
    E[l0+1,0] = -0.5*alpha0*(alpha0 + 1.)*(alpha0 - 2.)*ymax
    E[l0+2,0] = (1./6)*alpha0*(alpha0 + 1.)*(alpha0 - 1.)*ymax

  return E.tocsc()

def checkInputArgs4spdistr2D(zmax,x0,y0,Nx,Ny,p,eps):
  try:
	   zmax = float(zmax)
  except (ValueError,TypeError):
		raise Exception('argument zmax has to be a real number')
  try:
    x0 = float(x0)
    if not 0 <= x0 <= 1: raise ValueError
  except (ValueError,TypeError):
    raise Exception('argument x0 has to be a real number between 0 - 1')
  try:
    y0 = float(y0)
    if not 0 <= y0 <= 1: raise ValueError
  except (ValueError,TypeError):
    raise Exception('argument y0 has to be a real number between 0 - 1')
  try:
    Nx = int(Nx)
    if not Nx > 3: raise ValueError
  except (ValueError,TypeError):
    raise Exception('argument Nx has to be a positive integer greater than 3')
  try:
    Ny = int(Ny)
    if not Ny > 3: raise ValueError
  except (ValueError,TypeError):
    raise Exception('argument Ny has to be a positive integer greater than 3')
  if not p in ('trunc','bilin'):
    raise Exception('argument p has to be either "trunc" or "bilin"')
  try:
    eps = float(eps)
    if not eps > 0: raise ValueError
  except (ValueError,TypeError):
    raise Exception('argument eps has to be a real number greater than 0')

def spdistr2D(zmax,x0,y0,Nx,Ny,p='trunc',eps=1e-09,flatten=False):
  checkInputArgs4spdistr2D(zmax,x0,y0,Nx,Ny,p,eps)

  # derived parameters
  l0 = int(x0*Nx)                # integer horizontal observation index
  m0 = int(y0*Ny)                # integer vertical observation index
  alphax0 = x0*Nx - l0           # fractional remainder after truncation in horizontal direction
  alphay0 = y0*Ny - m0           # fractional remainder after truncation in vertical direction

  E = sp.lil_matrix((Nx,Ny))
  if p == 'trunc' or (alphax0 < eps and alphay0 < eps):
    E[l0,m0] = zmax
  else:
    E[l0,m0] = (1. - alphax0)*(1. - alphay0)*zmax
    E[l0,m0 + 1] = (1. - alphax0)*alphay0*zmax
    E[l0 + 1,m0] = alphax0*(1. - alphay0)*zmax
    E[l0 + 1,m0 + 1] = alphax0*alphay0*zmax

  # if true, flatten 2D matrix to 1D column vector by concatenating columns
  if flatten:
    tmp = E.copy()
    E = tmp[0,:].T
    for i in xrange(1,Nx):
      E = sp.vstack((E,tmp[i,:].T))

  return E.tocsc()

def spdistr2D_diag(zmax,x0,y0,Nx,Ny,epsilon=1,p='trunc',eps=1e-09):
  E = spdistr2D(zmax,x0,y0,Nx,Ny,epsilon,p,eps).tolil()

  J = sp.lil_matrix(((Nx + 1)*(Ny + 1),(Nx + 1)*(Ny + 1)))
  J[:Ny + 1,:Ny + 1] = E
  for i in xrange(1,Nx): J[i*(Ny + 1):(i + 1)*(Ny + 1),i*(Ny + 1):(i + 1)*(Ny + 1)] = E
  J[-Ny - 1:,-Ny - 1:] = E

  return J.tocsr()
