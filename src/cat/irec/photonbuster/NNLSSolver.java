package cat.irec.photonbuster;

/*
 * Copyright 2008 Josh Vermaas, except he's nice and instead prefers
 * this to be licensed under the LGPL. Since the license itself is longer
 * than the code, if this truly worries you, you can look up the text at
 * http://www.gnu.org/licenses/
 * 
 * Now onto the required "I didn't do it" part.
 * This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */
/**
 * This is a perfectly functional Java NNLS solver that utilizes JAMA for all
 * the Matrix calculations. As such, one will need Jama (currently at:
 * http://math.nist.gov/javanumerics/jama/) to use this program.
 * @author Josh Vermaas
 *
 */

import java.util.ArrayList;
import java.util.List;

public class NNLSSolver
{

	/**
	 * Whee!!! This is my own Java implementation of the NNLS algorithm
	 * as described in:
	 * Lawson and Hanson, "Solving Least Squares Problems", Prentice-Hall, 1974, Chapter 23, p. 161.
	 * It not only solves the least squares problem, but does so while also requiring
	 * that none of the answers be negative.
	 * @param A The A in Ax=b
	 * @param b The b in Ax=b
	 * @return The x in Ax=b
	 */
	public static Matrix solveNNLS(Matrix A,Matrix b)
	{
		List<Integer> p = new ArrayList<Integer>();
		List<Integer> z = new ArrayList<Integer>();
		int i = 0;
		int xm = A.getColumnDimension();
		int xn = 1;
		while (i < A.getColumnDimension()) z.add(i++);
		Matrix x = new Matrix(xm,xn);
		/*
		 * You need a finite number of iterations. Without this condition, the finite precision nature
		 * of the math being done almost makes certain that the <1e-15 conditions won't ever hold up.
		 * However, after so many iterations, it should at least be close to the correct answer.
		 * For the intrepid coder, however, one could replace this again with an infinite while
		 * loop and make the <1e-15 conditions into something like c*norm(A) or c*norm(b).
		 */
		for(int iterations = 0; iterations < 300*A.getColumnDimension()*A.getRowDimension(); iterations++)
		{
			//System.out.println(z.size() + " " + p.size());
			Matrix w = A.transpose().times(b.minus(A.times(x)));
			//w.print(7, 5);
			if(z.size() == 0 || isAllNegative(w))
			{
				//System.out.println("Computation should break");
				//We are done with the computation. Break here!
				break;//Should break out of the outer while loop.
			}
			//Step 4
			int t = z.get(0);
			double max = w.get(t, 0);
			for (i = 1; i < z.size(); i++)
			{
				if (w.get(z.get(i), 0) > max)
				{
					t = z.get(i);
					max = w.get(z.get(i), 0);
				}
			}
			//Step 5
			p.add(t);
			z.remove((Integer)t);
			boolean allPositive = false;
			while(!allPositive)
			{
				//Step 6
				Matrix Ep = new Matrix(b.getRowDimension(),p.size());
				for (i = 0; i < p.size(); i++)
					for (int j = 0; j < Ep.getRowDimension(); j++)
						Ep.set(j, i, A.get(j, p.get(i)));
				Matrix Zprime = Ep.solve(b);
				Ep = null;
				Matrix Z = new Matrix(xm,xn);
				for (i = 0; i < p.size(); i++)
					Z.set(p.get(i), 0, Zprime.get(i, 0));
				//Step 7
				allPositive = true;
				for (i = 0; i < p.size(); i++)
					allPositive &= Z.get(p.get(i), 0) > 0;
				if (allPositive)
					x = Z;
				else
				{
					double alpha = Double.MAX_VALUE;
					for (i = 0; i < p.size(); i++)
					{
						int q = p.get(i);
						if (Z.get(q,0) <= 0)
						{
							double xq = x.get(q, 0);
							if (xq / (xq - Z.get(q,0)) < alpha)
								alpha = xq / (xq - Z.get(q,0));
						}
					}
					//Finished getting alpha. Onto step 10
					x = x.plus(Z.minus(x).times(alpha));
					for (i = p.size() - 1; i >= 0; i--)
						if (Math.abs(x.get(p.get(i),0)) < 1e-15)//Close enough to zero, no?
							z.add(p.remove(i));
				}
			}
		}
		return x;
	}
	private static boolean isAllNegative(Matrix w)
	{
		boolean result = true;
		int m = w.getRowDimension();
		for (int i = 0; i < m; i++)
			result &= w.get(i, 0) <= 1e-15;
		return result;
	}
	
	public static class Matrix implements Cloneable, java.io.Serializable {

		/* ------------------------
		   Class variables
		 * ------------------------ */

		   /** Array for internal storage of elements.
		   @serial internal array storage.
		   */
		   private double[][] A;

		   /** Row and column dimensions.
		   @serial row dimension.
		   @serial column dimension.
		   */
		   private int m, n;

		/* ------------------------
		   Constructors
		 * ------------------------ */

		   /** Construct an m-by-n matrix of zeros. 
		   @param m    Number of rows.
		   @param n    Number of colums.
		   */

		   public Matrix (int m, int n) {
		      this.m = m;
		      this.n = n;
		      A = new double[m][n];
		   }

		   /** Construct an m-by-n constant matrix.
		   @param m    Number of rows.
		   @param n    Number of colums.
		   @param s    Fill the matrix with this scalar value.
		   */

		   public Matrix (int m, int n, double s) {
		      this.m = m;
		      this.n = n;
		      A = new double[m][n];
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            A[i][j] = s;
		         }
		      }
		   }

		   /** Construct a matrix from a 2-D array.
		   @param A    Two-dimensional array of doubles.
		   @exception  IllegalArgumentException All rows must have the same length
		   @see        #constructWithCopy
		   */

		   public Matrix (double[][] A) {
		      m = A.length;
		      n = A[0].length;
		      for (int i = 0; i < m; i++) {
		         if (A[i].length != n) {
		            throw new IllegalArgumentException("All rows must have the same length.");
		         }
		      }
		      this.A = A;
		   }

		   /** Construct a matrix quickly without checking arguments.
		   @param A    Two-dimensional array of doubles.
		   @param m    Number of rows.
		   @param n    Number of colums.
		   */

		   public Matrix (double[][] A, int m, int n) {
		      this.A = A;
		      this.m = m;
		      this.n = n;
		   }

		   /** Construct a matrix from a one-dimensional packed array
		   @param vals One-dimensional array of doubles, packed by columns (ala Fortran).
		   @param m    Number of rows.
		   @exception  IllegalArgumentException Array length must be a multiple of m.
		   */

		   public Matrix (double vals[], int m) {
		      this.m = m;
		      n = (m != 0 ? vals.length/m : 0);
		      if (m*n != vals.length) {
		         throw new IllegalArgumentException("Array length must be a multiple of m.");
		      }
		      A = new double[m][n];
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            A[i][j] = vals[i+j*m];
		         }
		      }
		   }

		/* ------------------------
		   Public Methods
		 * ------------------------ */

		   /** Construct a matrix from a copy of a 2-D array.
		   @param A    Two-dimensional array of doubles.
		   @exception  IllegalArgumentException All rows must have the same length
		   */

		   public static Matrix constructWithCopy(double[][] A) {
		      int m = A.length;
		      int n = A[0].length;
		      Matrix X = new Matrix(m,n);
		      double[][] C = X.getArray();
		      for (int i = 0; i < m; i++) {
		         if (A[i].length != n) {
		            throw new IllegalArgumentException
		               ("All rows must have the same length.");
		         }
		         for (int j = 0; j < n; j++) {
		            C[i][j] = A[i][j];
		         }
		      }
		      return X;
		   }

		   /** Make a deep copy of a matrix
		   */

		   public Matrix copy () {
		      Matrix X = new Matrix(m,n);
		      double[][] C = X.getArray();
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            C[i][j] = A[i][j];
		         }
		      }
		      return X;
		   }

		   /** Clone the Matrix object.
		   */

		   public Object clone () {
		      return this.copy();
		   }

		   /** Access the internal two-dimensional array.
		   @return     Pointer to the two-dimensional array of matrix elements.
		   */

		   public double[][] getArray () {
		      return A;
		   }

		   /** Copy the internal two-dimensional array.
		   @return     Two-dimensional array copy of matrix elements.
		   */

		   public double[][] getArrayCopy () {
		      double[][] C = new double[m][n];
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            C[i][j] = A[i][j];
		         }
		      }
		      return C;
		   }

		   /** Make a one-dimensional column packed copy of the internal array.
		   @return     Matrix elements packed in a one-dimensional array by columns.
		   */

		   public double[] getColumnPackedCopy () {
		      double[] vals = new double[m*n];
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            vals[i+j*m] = A[i][j];
		         }
		      }
		      return vals;
		   }

		   /** Make a one-dimensional row packed copy of the internal array.
		   @return     Matrix elements packed in a one-dimensional array by rows.
		   */

		   public double[] getRowPackedCopy () {
		      double[] vals = new double[m*n];
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            vals[i*n+j] = A[i][j];
		         }
		      }
		      return vals;
		   }

		   /** Get row dimension.
		   @return     m, the number of rows.
		   */

		   public int getRowDimension () {
		      return m;
		   }

		   /** Get column dimension.
		   @return     n, the number of columns.
		   */

		   public int getColumnDimension () {
		      return n;
		   }

		   /** Get a single element.
		   @param i    Row index.
		   @param j    Column index.
		   @return     A(i,j)
		   @exception  ArrayIndexOutOfBoundsException
		   */

		   public double get (int i, int j) {
		      return A[i][j];
		   }

		   /** Get a submatrix.
		   @param i0   Initial row index
		   @param i1   Final row index
		   @param j0   Initial column index
		   @param j1   Final column index
		   @return     A(i0:i1,j0:j1)
		   @exception  ArrayIndexOutOfBoundsException Submatrix indices
		   */

		   public Matrix getMatrix (int i0, int i1, int j0, int j1) {
		      Matrix X = new Matrix(i1-i0+1,j1-j0+1);
		      double[][] B = X.getArray();
		      try {
		         for (int i = i0; i <= i1; i++) {
		            for (int j = j0; j <= j1; j++) {
		               B[i-i0][j-j0] = A[i][j];
		            }
		         }
		      } catch(ArrayIndexOutOfBoundsException e) {
		         throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		      }
		      return X;
		   }

		   /** Get a submatrix.
		   @param r    Array of row indices.
		   @param c    Array of column indices.
		   @return     A(r(:),c(:))
		   @exception  ArrayIndexOutOfBoundsException Submatrix indices
		   */

		   public Matrix getMatrix (int[] r, int[] c) {
		      Matrix X = new Matrix(r.length,c.length);
		      double[][] B = X.getArray();
		      try {
		         for (int i = 0; i < r.length; i++) {
		            for (int j = 0; j < c.length; j++) {
		               B[i][j] = A[r[i]][c[j]];
		            }
		         }
		      } catch(ArrayIndexOutOfBoundsException e) {
		         throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		      }
		      return X;
		   }

		   /** Get a submatrix.
		   @param i0   Initial row index
		   @param i1   Final row index
		   @param c    Array of column indices.
		   @return     A(i0:i1,c(:))
		   @exception  ArrayIndexOutOfBoundsException Submatrix indices
		   */

		   public Matrix getMatrix (int i0, int i1, int[] c) {
		      Matrix X = new Matrix(i1-i0+1,c.length);
		      double[][] B = X.getArray();
		      try {
		         for (int i = i0; i <= i1; i++) {
		            for (int j = 0; j < c.length; j++) {
		               B[i-i0][j] = A[i][c[j]];
		            }
		         }
		      } catch(ArrayIndexOutOfBoundsException e) {
		         throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		      }
		      return X;
		   }

		   /** Get a submatrix.
		   @param r    Array of row indices.
		   @param j0   Initial column index
		   @param j1   Final column index
		   @return     A(r(:),j0:j1)
		   @exception  ArrayIndexOutOfBoundsException Submatrix indices
		   */

		   public Matrix getMatrix (int[] r, int j0, int j1) {
		      Matrix X = new Matrix(r.length,j1-j0+1);
		      double[][] B = X.getArray();
		      try {
		         for (int i = 0; i < r.length; i++) {
		            for (int j = j0; j <= j1; j++) {
		               B[i][j-j0] = A[r[i]][j];
		            }
		         }
		      } catch(ArrayIndexOutOfBoundsException e) {
		         throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		      }
		      return X;
		   }

		   /** Set a single element.
		   @param i    Row index.
		   @param j    Column index.
		   @param s    A(i,j).
		   @exception  ArrayIndexOutOfBoundsException
		   */

		   public void set (int i, int j, double s) {
		      A[i][j] = s;
		   }

		   /** Set a submatrix.
		   @param i0   Initial row index
		   @param i1   Final row index
		   @param j0   Initial column index
		   @param j1   Final column index
		   @param X    A(i0:i1,j0:j1)
		   @exception  ArrayIndexOutOfBoundsException Submatrix indices
		   */

		   public void setMatrix (int i0, int i1, int j0, int j1, Matrix X) {
		      try {
		         for (int i = i0; i <= i1; i++) {
		            for (int j = j0; j <= j1; j++) {
		               A[i][j] = X.get(i-i0,j-j0);
		            }
		         }
		      } catch(ArrayIndexOutOfBoundsException e) {
		         throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		      }
		   }

		   /** Set a submatrix.
		   @param r    Array of row indices.
		   @param c    Array of column indices.
		   @param X    A(r(:),c(:))
		   @exception  ArrayIndexOutOfBoundsException Submatrix indices
		   */

		   public void setMatrix (int[] r, int[] c, Matrix X) {
		      try {
		         for (int i = 0; i < r.length; i++) {
		            for (int j = 0; j < c.length; j++) {
		               A[r[i]][c[j]] = X.get(i,j);
		            }
		         }
		      } catch(ArrayIndexOutOfBoundsException e) {
		         throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		      }
		   }

		   /** Set a submatrix.
		   @param r    Array of row indices.
		   @param j0   Initial column index
		   @param j1   Final column index
		   @param X    A(r(:),j0:j1)
		   @exception  ArrayIndexOutOfBoundsException Submatrix indices
		   */

		   public void setMatrix (int[] r, int j0, int j1, Matrix X) {
		      try {
		         for (int i = 0; i < r.length; i++) {
		            for (int j = j0; j <= j1; j++) {
		               A[r[i]][j] = X.get(i,j-j0);
		            }
		         }
		      } catch(ArrayIndexOutOfBoundsException e) {
		         throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		      }
		   }

		   /** Set a submatrix.
		   @param i0   Initial row index
		   @param i1   Final row index
		   @param c    Array of column indices.
		   @param X    A(i0:i1,c(:))
		   @exception  ArrayIndexOutOfBoundsException Submatrix indices
		   */

		   public void setMatrix (int i0, int i1, int[] c, Matrix X) {
		      try {
		         for (int i = i0; i <= i1; i++) {
		            for (int j = 0; j < c.length; j++) {
		               A[i][c[j]] = X.get(i-i0,j);
		            }
		         }
		      } catch(ArrayIndexOutOfBoundsException e) {
		         throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		      }
		   }

		   /** Matrix transpose.
		   @return    A'
		   */

		   public Matrix transpose () {
		      Matrix X = new Matrix(n,m);
		      double[][] C = X.getArray();
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            C[j][i] = A[i][j];
		         }
		      }
		      return X;
		   }

		   /** One norm
		   @return    maximum column sum.
		   */

		   public double norm1 () {
		      double f = 0;
		      for (int j = 0; j < n; j++) {
		         double s = 0;
		         for (int i = 0; i < m; i++) {
		            s += Math.abs(A[i][j]);
		         }
		         f = Math.max(f,s);
		      }
		      return f;
		   }

		   /** Infinity norm
		   @return    maximum row sum.
		   */

		   public double normInf () {
		      double f = 0;
		      for (int i = 0; i < m; i++) {
		         double s = 0;
		         for (int j = 0; j < n; j++) {
		            s += Math.abs(A[i][j]);
		         }
		         f = Math.max(f,s);
		      }
		      return f;
		   }

		   /**  Unary minus
		   @return    -A
		   */

		   public Matrix uminus () {
		      Matrix X = new Matrix(m,n);
		      double[][] C = X.getArray();
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            C[i][j] = -A[i][j];
		         }
		      }
		      return X;
		   }

		   /** C = A + B
		   @param B    another matrix
		   @return     A + B
		   */

		   public Matrix plus (Matrix B) {
		      checkMatrixDimensions(B);
		      Matrix X = new Matrix(m,n);
		      double[][] C = X.getArray();
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            C[i][j] = A[i][j] + B.A[i][j];
		         }
		      }
		      return X;
		   }

		   /** A = A + B
		   @param B    another matrix
		   @return     A + B
		   */

		   public Matrix plusEquals (Matrix B) {
		      checkMatrixDimensions(B);
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            A[i][j] = A[i][j] + B.A[i][j];
		         }
		      }
		      return this;
		   }

		   /** C = A - B
		   @param B    another matrix
		   @return     A - B
		   */

		   public Matrix minus (Matrix B) {
		      checkMatrixDimensions(B);
		      Matrix X = new Matrix(m,n);
		      double[][] C = X.getArray();
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            C[i][j] = A[i][j] - B.A[i][j];
		         }
		      }
		      return X;
		   }

		   /** A = A - B
		   @param B    another matrix
		   @return     A - B
		   */

		   public Matrix minusEquals (Matrix B) {
		      checkMatrixDimensions(B);
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            A[i][j] = A[i][j] - B.A[i][j];
		         }
		      }
		      return this;
		   }

		   /** Element-by-element multiplication, C = A.*B
		   @param B    another matrix
		   @return     A.*B
		   */

		   public Matrix arrayTimes (Matrix B) {
		      checkMatrixDimensions(B);
		      Matrix X = new Matrix(m,n);
		      double[][] C = X.getArray();
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            C[i][j] = A[i][j] * B.A[i][j];
		         }
		      }
		      return X;
		   }

		   /** Element-by-element multiplication in place, A = A.*B
		   @param B    another matrix
		   @return     A.*B
		   */

		   public Matrix arrayTimesEquals (Matrix B) {
		      checkMatrixDimensions(B);
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            A[i][j] = A[i][j] * B.A[i][j];
		         }
		      }
		      return this;
		   }

		   /** Element-by-element right division, C = A./B
		   @param B    another matrix
		   @return     A./B
		   */

		   public Matrix arrayRightDivide (Matrix B) {
		      checkMatrixDimensions(B);
		      Matrix X = new Matrix(m,n);
		      double[][] C = X.getArray();
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            C[i][j] = A[i][j] / B.A[i][j];
		         }
		      }
		      return X;
		   }

		   /** Element-by-element right division in place, A = A./B
		   @param B    another matrix
		   @return     A./B
		   */

		   public Matrix arrayRightDivideEquals (Matrix B) {
		      checkMatrixDimensions(B);
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            A[i][j] = A[i][j] / B.A[i][j];
		         }
		      }
		      return this;
		   }

		   /** Element-by-element left division, C = A.\B
		   @param B    another matrix
		   @return     A.\B
		   */

		   public Matrix arrayLeftDivide (Matrix B) {
		      checkMatrixDimensions(B);
		      Matrix X = new Matrix(m,n);
		      double[][] C = X.getArray();
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            C[i][j] = B.A[i][j] / A[i][j];
		         }
		      }
		      return X;
		   }

		   /** Element-by-element left division in place, A = A.\B
		   @param B    another matrix
		   @return     A.\B
		   */

		   public Matrix arrayLeftDivideEquals (Matrix B) {
		      checkMatrixDimensions(B);
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            A[i][j] = B.A[i][j] / A[i][j];
		         }
		      }
		      return this;
		   }

		   /** Multiply a matrix by a scalar, C = s*A
		   @param s    scalar
		   @return     s*A
		   */

		   public Matrix times (double s) {
		      Matrix X = new Matrix(m,n);
		      double[][] C = X.getArray();
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            C[i][j] = s*A[i][j];
		         }
		      }
		      return X;
		   }

		   /** Multiply a matrix by a scalar in place, A = s*A
		   @param s    scalar
		   @return     replace A by s*A
		   */

		   public Matrix timesEquals (double s) {
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            A[i][j] = s*A[i][j];
		         }
		      }
		      return this;
		   }

		   /** Linear algebraic matrix multiplication, A * B
		   @param B    another matrix
		   @return     Matrix product, A * B
		   @exception  IllegalArgumentException Matrix inner dimensions must agree.
		   */

		   public Matrix times (Matrix B) {
		      if (B.m != n) {
		         throw new IllegalArgumentException("Matrix inner dimensions must agree.");
		      }
		      Matrix X = new Matrix(m,B.n);
		      double[][] C = X.getArray();
		      double[] Bcolj = new double[n];
		      for (int j = 0; j < B.n; j++) {
		         for (int k = 0; k < n; k++) {
		            Bcolj[k] = B.A[k][j];
		         }
		         for (int i = 0; i < m; i++) {
		            double[] Arowi = A[i];
		            double s = 0;
		            for (int k = 0; k < n; k++) {
		               s += Arowi[k]*Bcolj[k];
		            }
		            C[i][j] = s;
		         }
		      }
		      return X;
		   }

		   /** Solve A*X = B
		   @param B    right hand side
		   @return     solution if A is square, least squares solution otherwise
		   */

		   public Matrix solve (Matrix B) {
		      return (m == n ? (new LUDecomposition(this)).solve(B) : (new QRDecomposition(this)).solve(B));
		   }

		   /** Solve X*A = B, which is also A'*X' = B'
		   @param B    right hand side
		   @return     solution if A is square, least squares solution otherwise.
		   */

		   public Matrix solveTranspose (Matrix B) {
		      return transpose().solve(B.transpose());
		   }

		   /** Matrix inverse or pseudoinverse
		   @return     inverse(A) if A is square, pseudoinverse otherwise.
		   */

		   public Matrix inverse () {
		      return solve(identity(m,m));
		   }

		   /** Matrix determinant
		   @return     determinant
		   */

		   public double det () {
		      return new LUDecomposition(this).det();
		   }

		   /** Matrix trace.
		   @return     sum of the diagonal elements.
		   */

		   public double trace () {
		      double t = 0;
		      for (int i = 0; i < Math.min(m,n); i++) {
		         t += A[i][i];
		      }
		      return t;
		   }

		   /** Generate matrix with random elements
		   @param m    Number of rows.
		   @param n    Number of colums.
		   @return     An m-by-n matrix with uniformly distributed random elements.
		   */

		   public static Matrix random (int m, int n) {
		      Matrix A = new Matrix(m,n);
		      double[][] X = A.getArray();
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            X[i][j] = Math.random();
		         }
		      }
		      return A;
		   }

		   /** Generate identity matrix
		   @param m    Number of rows.
		   @param n    Number of colums.
		   @return     An m-by-n matrix with ones on the diagonal and zeros elsewhere.
		   */

		   public static Matrix identity (int m, int n) {
		      Matrix A = new Matrix(m,n);
		      double[][] X = A.getArray();
		      for (int i = 0; i < m; i++) {
		         for (int j = 0; j < n; j++) {
		            X[i][j] = (i == j ? 1.0 : 0.0);
		         }
		      }
		      return A;
		   }
		
		/* ------------------------
		   Private Methods
		 * ------------------------ */

		   /** Check if size(A) == size(B) **/

		   private void checkMatrixDimensions (Matrix B) {
		      if (B.m != m || B.n != n) {
		         throw new IllegalArgumentException("Matrix dimensions must agree.");
		      }
		   }

		  private static final long serialVersionUID = 1;
	}
}
