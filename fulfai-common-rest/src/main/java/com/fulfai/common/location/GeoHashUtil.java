package com.fulfai.common.location;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for geohash encoding/decoding and distance calculations.
 */
public class GeoHashUtil {

    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int DEFAULT_PRECISION = 6;

    public static String encode(double latitude, double longitude) {
        return encode(latitude, longitude, DEFAULT_PRECISION);
    }

    public static String encode(double latitude, double longitude, int precision) {
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};

        StringBuilder geohash = new StringBuilder();
        boolean isEven = true;
        int bit = 0;
        int ch = 0;

        while (geohash.length() < precision) {
            if (isEven) {
                double mid = (lonRange[0] + lonRange[1]) / 2;
                if (longitude >= mid) {
                    ch |= (1 << (4 - bit));
                    lonRange[0] = mid;
                } else {
                    lonRange[1] = mid;
                }
            } else {
                double mid = (latRange[0] + latRange[1]) / 2;
                if (latitude >= mid) {
                    ch |= (1 << (4 - bit));
                    latRange[0] = mid;
                } else {
                    latRange[1] = mid;
                }
            }

            isEven = !isEven;
            if (bit < 4) {
                bit++;
            } else {
                geohash.append(BASE32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }

        return geohash.toString();
    }

    public static List<String> getNeighbors(String geohash) {
        List<String> neighbors = new ArrayList<>();
        neighbors.add(geohash);
        neighbors.add(getAdjacent(geohash, "n"));
        neighbors.add(getAdjacent(geohash, "s"));
        neighbors.add(getAdjacent(geohash, "e"));
        neighbors.add(getAdjacent(geohash, "w"));
        neighbors.add(getAdjacent(getAdjacent(geohash, "n"), "e"));
        neighbors.add(getAdjacent(getAdjacent(geohash, "n"), "w"));
        neighbors.add(getAdjacent(getAdjacent(geohash, "s"), "e"));
        neighbors.add(getAdjacent(getAdjacent(geohash, "s"), "w"));
        return neighbors;
    }

    private static String getAdjacent(String geohash, String direction) {
        if (geohash == null || geohash.isEmpty()) {
            return "";
        }

        String[][] neighbors = {
            {"p0r21436x8zb9dcf5h7kjnmqesgutwvy", "bc01fg45238967deuvhjyznpkmstqrwx"},
            {"bc01fg45238967deuvhjyznpkmstqrwx", "p0r21436x8zb9dcf5h7kjnmqesgutwvy"}
        };

        String[][] borders = {
            {"prxz", "bcfguvyz"},
            {"bcfguvyz", "prxz"}
        };

        int dirIndex = direction.equals("n") || direction.equals("s") ? 0 : 1;
        int typeIndex = direction.equals("n") || direction.equals("e") ? 0 : 1;

        char lastChar = geohash.charAt(geohash.length() - 1);
        String parent = geohash.substring(0, geohash.length() - 1);

        if (borders[dirIndex][typeIndex].indexOf(lastChar) != -1 && !parent.isEmpty()) {
            parent = getAdjacent(parent, direction);
        }

        int charIndex = neighbors[dirIndex][typeIndex].indexOf(lastChar);
        return parent + BASE32.charAt(charIndex);
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
