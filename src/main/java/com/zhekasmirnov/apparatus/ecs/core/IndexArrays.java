package com.zhekasmirnov.apparatus.ecs.core;

import java.util.Arrays;

public class IndexArrays {
    // hashes

    public static int arrayHash(int[] indices, int count) {
        int result = 1;
        for (int i = 0; i < count; i++)
            result = 31 * result + indices[i];
        return result;
    }

    public static int arrayAndItemHash(int[] indices, int count, int newItem) {
        int result = 1;
        int i = 0, val = -1;
        for (;i < count; i++) {
            val = indices[i];
            if (val >= newItem) {
                break;
            }
            result = 31 * result + val;
        }
        if (val != newItem) {
            result = 31 * result + newItem;
        }
        for (;i < count; i++) {
            result = 31 * result + indices[i];
        }
        return result;
    }

    public static int unionHash(int[] indices1, int count1, int[] indices2, int count2) {
        int result = 1;
        int i = 0, j = 0;
        while (i < count1 & j < count2) {
            int e1 = indices1[i];
            int e2 = indices2[j];
            if (e1 < e2) {
                result = 31 * result + e1;
                i++;
            } else if (e1 > e2) {
                result = 31 * result + e2;
                j++;
            } else {
                result = 31 * result + e1;
                i++;
                j++;
            }
        }
        while (i < count1)
            result = 31 * result + indices1[i++];
        while (j < count2)
            result = 31 * result + indices2[j++];
        return result;
    }

    public static int diffHash(int[] indices1, int count1, int[] indices2, int count2) {
        int result = 1;
        int i = 0, j = 0;
        while (i < count1 && j < count2) {
            int e1 = indices1[i];
            int e2 = indices2[j];
            if (e1 < e2) {
                i++;
                result = 31 * result + e1;
            } else if (e1 > e2) {
                j++;
            } else {
                i++;
                j++;
            }
        }
        while (i < count1)
            result = 31 * result + indices1[i++];
        return result;
    }

    // indices1 + indices2 - indices3 !!! BUT indices2 and indices3 must not overlap!
    public static int unionAndDiffHash(int[] indices1, int count1, int[] indices2, int count2, int[] indices3, int count3) {
        int result = 1;
        int i = 0, j = 0, k = 0;
        while (i < count1 && j < count2) {
            int e1 = indices1[i];
            int e2 = indices2[j];
            int e3 = -1; // -
            while (k < count3 && (e3 = indices3[k]) < e1) {
                k++;
            }
            if (e1 > e2) {
                result = 31 * result + e2;
                j++;
            }
            if (e1 == e2) {
                result = 31 * result + e1;
                j++;
                i++;
            } else if (e1 == e3) {
                k++;
                i++;
            } else {
                result = 31 * result + e1;
                i++;
            }
        }

        while (i < count1)
            result = 31 * result + indices1[i++];
        while (j < count2)
            result = 31 * result + indices2[j++];

        return result;
    }

    // compare

    public static boolean arrayMatch(int[] indices, int count, int[] otherIndices, int otherCount) {
        if (otherCount != count) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            if (indices[i] != otherIndices[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean arrayAndItemMatch(int[] indices, int count, int[] otherIndices, int otherCount, int newItem) {
        if (otherCount != count && otherCount != count + 1) {
            return false;
        }

        for (int i = 0, j = 0; i < count; i++, j++) {
            int val = indices[i];
            if (val >= newItem) {
                if (val != newItem)
                    return false;
                newItem = Integer.MAX_VALUE;
                j--;
            }
            if (val != indices[j])
                return false;
        }

        return newItem == Integer.MAX_VALUE;
    }

    public static boolean unionMatch(int[] indices, int count, int[] indices1, int count1, int[] indices2, int count2) {
        if (count < count1 | count < count2 | count > count1 + count2)
            return false;

        int i = 0, j = 0, k = 0;
        while (i < count1 & j < count2 & k < count) {
            int e1 = indices1[i];
            int e2 = indices2[j];
            if (e1 < e2) {
                if (indices[k++] != e1)
                    return false;
                i++;
            } else if (e1 > e2) {
                if (indices[k++] != e2)
                    return false;
                j++;
            } else {
                if (indices[k++] != e1)
                    return false;
                i++;
                j++;
            }
        }

        if (k != count - (count1 - i) - (count2 - j))
            return false;
        while (i < count1)
            if (indices1[i++] != indices[k++])
                return false;
        while (j < count2)
            if (indices2[j++] != indices[k++])
                return false;
        return true;
    }

    public static boolean diffMatch(int[] indices, int count, int[] indices1, int count1, int[] indices2, int count2) {
        if (count > count1 || count < count1 - count2)
            return false;

        int i = 0, j = 0, k = 0;
        while (i < count1 && j < count2 && k < count) {
            int e1 = indices1[i];
            int e2 = indices2[j];
            if (e1 < e2) {
                if (indices[k++] != e1)
                    return false;
                i++;
            } else if (e1 > e2) {
                j++;
            } else {
                i++;
                j++;
            }
        }

        while (i < count1 && j < count2) {
            int e1 = indices1[i];
            int e2 = indices2[j];
            if (e1 > e2) {
                j++;
            } else if (e1 < e2) {
                return false;
            } else {
                i++;
                j++;
            }
        }

        if (k != count - (count1 - i))
            return false;

        while (i < count1)
            if (indices1[i++] != indices[k++])
                return false;

        return true;
    }

    // indices == indices1 + indices2 - indices3 !!! BUT indices2 and indices3 must not overlap!
    public static boolean unionAndDiffMatch(int[] indices, int count, int[] indices1, int count1, int[] indices2, int count2, int[] indices3, int count3) {
        int i = 0, j = 0, k = 0, n = 0;
        while (i < count1 && j < count2) {
            int e1 = indices1[i];
            int e2 = indices2[j];
            int e3 = -1; // -
            while (k < count3 && (e3 = indices3[k]) < e1) {
                k++;
            }
            if (e1 > e2) {
                if (indices[n++] != e2)
                    return false;
                j++;
            }
            if (e1 == e2) {
                if (indices[n++] != e1)
                    return false;
                j++;
                i++;
            } else if (e1 == e3) {
                k++;
                i++;
            } else {
                if (indices[n++] != e1)
                    return false;
                i++;
            }
        }

        if (count != n + (count1 - i) + (count2 - i))
            return false;

        while (i < count1)
            if (indices[n++] != indices1[i++])
                return false;
        while (j < count2)
            if (indices[n++] != indices2[j++])
                return false;
        return true;
    }

    // operations

    public static int[] addItemToArray(int[] indices, int count, int newItem) {
        int lower = 0;
        int upper = count - 1;
        while (lower <= upper) {
            int mid = (lower + upper) >> 2;
            int midValue = indices[mid];
            if (midValue == newItem) {
                int[] result = Arrays.copyOf(indices, indices.length);
                result[mid] = newItem;
                return result;
            } else if (midValue > newItem) {
                upper = mid - 1;
            } else {
                lower = mid + 1;
            }
        }

        int insertIndex = lower;
        int[] result = new int[count + 1];
        System.arraycopy(indices, 0, result, 0, insertIndex);
        result[insertIndex] = newItem;
        System.arraycopy(indices, insertIndex + 1, result, insertIndex + 1, count - insertIndex - 1);
        return result;
    }

    public static int[] arrayUnion(int[] indices1, int count1, int[] indices2, int count2) {
        int newCount = 0;
        {
            int i = 0, j = 0;
            while (i < count1 && j < count2) {
                int e1 = indices1[i];
                int e2 = indices2[j];
                if (e2 < e1) {
                    j++;
                } else if (e2 > e1) {
                    i++;
                } else {
                    i++;
                    j++;
                }
                newCount++;
            }
            newCount += (count2 - j) + (count1 - i);
        }

        int[] result = new int[newCount];
        {
            int i = 0, j = 0, k = 0;
            while (i < count1 && j < count2) {
                int e1 = indices1[i];
                int e2 = indices2[j];
                if (e2 < e1) {
                    result[k++] = e2;
                    j++;
                } else if (e2 > e1) {
                    result[k++] = e1;
                    i++;
                } else {
                    result[k++] = e1;
                    i++;
                    j++;
                }
                newCount++;
            }
            while (i < count1)
                result[k++] = indices1[i++];
            while (j < count2)
                result[k++] = indices2[j++];
        }
        return result;
    }

    public static int[] arrayDiff(int[] indices1, int count1, int[] indices2, int count2) {
        int newCount = 0;
        {
            int i = 0, j = 0;
            while (i < count1 && j < count2) {
                int e1 = indices1[i];
                int e2 = indices2[j];
                if (e1 < e2) {
                    i++;
                    newCount++;
                } else if (e1 > e2) {
                    j++;
                } else {
                    i++;
                    j++;
                }
            }
            newCount += count1 - i;
        }

        int[] result = new int[newCount];
        {
            int i = 0, j = 0, k = 0;
            while (i < count1 && j < count2) {
                int e1 = indices1[i];
                int e2 = indices2[j];
                if (e1 < e2) {
                    i++;
                    result[k++] = e1;
                } else if (e1 > e2) {
                    j++;
                } else {
                    i++;
                    j++;
                }
            }
            while (i < count1)
                result[k++] = indices1[i++];
        }
        return result;
    }

    // indices1 + indices2 - indices3 !!! BUT indices2 and indices3 must not overlap!
    public static int[] arrayUnionAndDiff(int[] indices1, int count1, int[] indices2, int count2, int[] indices3, int count3) {
        int newCount = 0;
        {
            int i = 0, j = 0, k = 0;
            while (i < count1 && j < count2) {
                int e1 = indices1[i];
                int e2 = indices2[j];
                int e3 = -1; // -
                while (k < count3 && (e3 = indices3[k]) < e1) {
                    k++;
                }
                if (e1 > e2) {
                    newCount++;
                    j++;
                }
                if (e1 == e2) {
                    newCount++;
                    j++;
                    i++;
                } else if (e1 == e3) {
                    k++;
                    i++;
                } else {
                    i++;
                    newCount++;
                }
            }

            newCount += (count1 - i) + (count2 - j);
        }

        int[] result = new int[newCount];
        {
            int i = 0, j = 0, k = 0, n = 0;
            while (i < count1 && j < count2) {
                int e1 = indices1[i];
                int e2 = indices2[j];
                int e3 = -1; // -
                while (k < count3 && (e3 = indices3[k]) < e1) {
                    k++;
                }
                if (e1 > e2) {
                    result[n++] = e2;
                    j++;
                }
                if (e1 == e2) {
                    result[n++] = e1;
                    j++;
                    i++;
                } else if (e1 == e3) {
                    k++;
                    i++;
                } else {
                    result[n++] = e1;
                    i++;
                }
            }

            while (i < count1)
                result[n++] = indices1[i++];
            while (j < count2)
                result[n++] = indices2[j++];
        }
        return result;
    }
}
