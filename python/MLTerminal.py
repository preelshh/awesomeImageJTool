#!/usr/bin/env python
# coding: utf-8

# In[1]:


#!/usr/bin/env python
# coding: utf-8

# In[1]:


#!/usr/bin/env python
# coding: utf-8
#!/usr/bin/env python
# coding: utf-8
#!/usr/bin/env python
# coding: utf-8

import sys
import os
import pandas as pd
import numpy as np
import joblib
import subprocess

# ──────────────────────────────────────────────────────────────
# Load classifier and assets
# ──────────────────────────────────────────────────────────────

# Get the folder where this Python file is located
base_dir = os.path.dirname(os.path.abspath(__file__))

# Build paths to your assets
clf_path = os.path.join(base_dir, "my_classifier.pkl")
le_path = os.path.join(base_dir, "label_encoder.pkl")
feat_path = os.path.join(base_dir, "trained_features.pkl")

# Load them
clf = joblib.load(clf_path)
le = joblib.load(le_path)
trained_features = joblib.load(feat_path)

# ──────────────────────────────────────────────────────────────
# Scoring interpretation
# ──────────────────────────────────────────────────────────────
def interpret_class_score(score):
    if score < 1.5:
        return "Mostly Class A: elongated, sparse, low-branching particles (e.g., filamentous or thread-like)."
    elif 1.5 <= score < 2.0:
        return "Mostly A to B: mixture of elongated shapes and some irregular, branching structures — transitioning from sparse to moderately dispersed."
    elif 2.0 <= score < 2.5:
        return "Mostly B to C: increasingly irregular and clump-forming — complex, branching particles."
    elif 2.5 <= score < 3.0:
        return "Mostly Class C: dense clumps with heterogeneous shapes — compact but not spherical."
    elif 3.0 <= score < 3.5:
        return "Mostly C to D: clumpy to rounded transitions — mix of irregular aggregates and pellet-like structures."
    else:
        return "Mostly Class D: dense, rounded, compact particles — likely pellet morphology."

# ──────────────────────────────────────────────────────────────
# Size Label Determination
# ──────────────────────────────────────────────────────────────
def assign_size_label(area, diam):
    area_s = 10000 * area ## This is done to convert the measurements from um to cm
    diam_s = 10000 * diam ## This is done to convert the measurements from um to cm

    ## Simple logic for if a particle is within a certain AREA threshold or DIAMETER threshold
    if area_s <= 1.2 * 10**7 or diam_s <= 400:
        return "S"
    elif area_s <= 2.8 * 10**7 or diam_s <= 600:
        return "M"
    elif area_s <= 8 * 10**7 or diam_s <= 1000:
        return "L"
    elif area_s > 8 * 10**7 or diam_s > 1000:
        return "XL"
    else:
        return "Ambig." ## Most likely will never return Ambig.

# ──────────────────────────────────────────────────────────────
# Size Factor Determination
# ──────────────────────────────────────────────────────────────
def assign_size_factor(cl, sz):
    ## cl is class and sz is the size factor produced from the 'size label determination' function
    if cl == "A" or cl == "B": 
        if sz == "S":
            return 0.2
        elif sz == "M":
            return 0.3
        elif sz == "L":
            return 0.4
        elif sz == "XL":
            return 0.5
    elif cl == "C" or cl == "D":
        if sz == "S":
            return 0.5
        elif sz == "M":
            return 0.4
        elif sz == "L":
            return 0.3
        elif sz == "XL":
            return 0.2
    return None  # in case input is invalid

                    
# ──────────────────────────────────────────────────────────────
# Fix column names + Feature Engineering
# ──────────────────────────────────────────────────────────────
def fix_csv_file(df):
    df.columns = df.columns.str.replace(",", "", regex=False).str.strip()
    rename_map = {
        "Area": "Area", "Perimeter": "Perimeter", "Ellipse.Center.X": "Ellipse.Center.X",
        "Ellipse.Center.Y": "Ellipse.Center.Y", "Ellipse.Radius1": "Ellipse.Radius1",
        "Ellipse.Radius2": "Ellipse.Radius2", "Ellipse.Orientation": "Ellipse.Orientation",
        "InscrCircle.Center.X": "InscrDisc.Center.X", "InscrCircle.Center.Y": "InscrDisc.Center.Y",
        "InscrCircle.Radius": "InscrDisc.Radius", "Diameter": "MaxFeretDiam",
        "ConvexArea": "ConvexArea", "Convexity": "Convexity", "GeodesicDiameter": "GeodesicDiameter",
        "Box2D.XMin": "Box.X.Min", "Box2D.XMax": "Box.X.Max", "Box2D.YMin": "Box.Y.Min",
        "Box2D.YMax": "Box.Y.Max", "Centroid.X": "Centroid.X", "Centroid.Y": "Centroid.Y",
        "GeodesicElongation": "GeodesicElongation", "P1.x": "Obox.1.X", "P1.y": "Obox.1.Y",
        "P2.x": "Obox.2.X", "p2.y": "Obox.2.Y", "Orientation": "OBox.Orientation"
    }
    df = df.rename(columns=rename_map)

    # Derived features
    df['Circularity'] = 4 * np.pi * df['Area'] / (df['Perimeter'] ** 2)
    df['Tortuosity'] = df['GeodesicDiameter'] / df['MaxFeretDiam']
    df['Solidity'] = df['Area'] / df['ConvexArea']
    df['OBox.Length'] = (df['Obox.2.X'] - df['Obox.1.X']).abs()
    df['OBox.Width'] = (df['Obox.2.Y'] - df['Obox.1.Y']).abs()
    df['OBox.Center.X'] = (df['Obox.1.X'] + df['Obox.2.X']) / 2
    df['OBox.Center.Y'] = (df['Obox.1.Y'] + df['Obox.2.Y']) / 2
    df['Ellipse.Elong'] = df[['Ellipse.Radius1', 'Ellipse.Radius2']].max(axis=1) / df[['Ellipse.Radius1', 'Ellipse.Radius2']].min(axis=1)
    df['MaxFeretDiamAngle'] = np.degrees(np.arctan2(df['Obox.2.Y'] - df['Obox.1.Y'], df['Obox.2.X'] - df['Obox.1.X']))
    df['Perimeter_Area_Ratio'] = df['Perimeter'] / df['Area']

    keep_cols = [
        'Area', 'Perimeter', 'Circularity', 'Box.X.Min', 'Box.X.Max',
        'Box.Y.Min', 'Box.Y.Max', 'Centroid.X', 'Centroid.Y',
        'Ellipse.Center.X', 'Ellipse.Center.Y', 'Ellipse.Radius1',
        'Ellipse.Radius2', 'Ellipse.Orientation', 'Ellipse.Elong', 'ConvexArea',
        'Convexity', 'MaxFeretDiam', 'MaxFeretDiamAngle', 'OBox.Center.X',
        'OBox.Center.Y', 'OBox.Length', 'OBox.Width',
        'OBox.Orientation', 'GeodesicDiameter', 'Tortuosity',
        'InscrDisc.Radius', 'GeodesicElongation', 'Perimeter_Area_Ratio', 'Solidity'
    ]

    df = df[[col for col in keep_cols if col in df.columns]]


    ## change measurements
    df = df[df['Area'] > 0.0025]
    return df

# ──────────────────────────────────────────────────────────────
# Classify particles using ML
# ──────────────────────────────────────────────────────────────
def classify_df(df):
    df_model = df[[col for col in trained_features if col in df.columns]].fillna(0)
    y_pred = clf.predict(df_model)
    y_labels = le.inverse_transform(y_pred)
    df['Class'] = y_labels
    df['predicted_numeric'] = df['Class'].map({'A': 1, 'B': 2, 'C': 3, 'D': 4})
    return df

# ──────────────────────────────────────────────────────────────
# Run pipeline on one image's particle DataFrame
# ──────────────────────────────────────────────────────────────

def run_on_one_df(df, path, super_df):
    df = fix_csv_file(df)
    df = classify_df(df)
    df['Image_Source'] = os.path.basename(path)
    df['Size_Label'] = df.apply(
        lambda row: assign_size_label(row['Area'], row['MaxFeretDiam']), axis=1
    )
    df['Size_Factor'] = df.apply(
        lambda row: assign_size_factor(row['Class'], row['Size_Label']),
        axis=1 )
    
    df['Size_Factor_A'] = df.apply(
        lambda row: row['Size_Factor'] if row['Class'] == 'A' else 0,
        axis=1
    )
    df['Size_Factor_B'] = df.apply(
        lambda row: row['Size_Factor'] if row['Class'] == 'B' else 0,
        axis=1
    )
    
    df['Size_Factor_C'] = df.apply(
        lambda row: row['Size_Factor'] if row['Class'] == 'C' else 0,
        axis=1
    )
    
    df['Size_Factor_D'] = df.apply(
        lambda row: row['Size_Factor'] if row['Class'] == 'D' else 0,
        axis=1
    )
    
    
    super_df.append(df)
    

# ──────────────────────────────────────────────────────────────
# Summarize all images → Output final CSV
# ──────────────────────────────────────────────────────────────
def create_output_csv(df, out_path):
    classes = ['A', 'B', 'C', 'D']
    df['Class'] = pd.Categorical(df['Class'], categories=classes)
    class_counts = pd.crosstab(df['Image_Source'], df['Class'])
    class_counts['Amount of Particles'] = class_counts.sum(axis=1)

    for cls in ['A', 'B', 'C', 'D']:
        class_counts[f'Percentage of Class {cls}'] = (
            class_counts.get(cls, 0) / class_counts['Amount of Particles']
        ) * 100
        class_counts.rename(columns={cls: f'Amount of Class {cls}'}, inplace=True)

    overall_means = df.groupby('Image_Source', observed=False).agg({
        'Area': 'mean',
        'Perimeter': 'mean',
        'GeodesicElongation': 'mean',
        'Convexity': 'mean',
        'Solidity': 'mean',
        'MaxFeretDiam': 'mean',
        'Circularity': 'mean',
        'Perimeter_Area_Ratio': 'mean'
        
    }).rename(columns={
        'Area': 'Mean Total Area',
        'Perimeter': 'Mean Total Perimeter',
        'GeodesicElongation': 'Mean Geodesic Elongation',
        'Convexity': 'Mean Convexity',
        'Solidity': 'Mean Solidity',
        'MaxFeretDiam': 'Mean MaxFeretDiam',
        'Circularity': 'Mean Circularity',
        'Perimeter_Area_Ratio' : 'Mean Perimeter_Area_Ratio'
        
    })

    total_area = df.groupby('Image_Source', observed=False)['Area'].sum().rename("Total Area")


    class_area_totals = (
        df.groupby(['Image_Source', 'Class'], observed=False)['Area'].sum()
        .unstack()
        .fillna(0)
        .rename(columns={cls: f"Class {cls} Area" for cls in ['A', 'B', 'C', 'D']})
    )

    #  Compute percentage area per class
    for cls in ['A', 'B', 'C', 'D']:
        class_area_totals[f"Class {cls} Area %"] = (
            class_area_totals.get(f"Class {cls} Area", 0) / total_area
        ) * 100

    class_specific = df.groupby(['Image_Source', 'Class'], observed=False).agg({
        'Area': 'mean',
        'Perimeter': 'mean',
        'GeodesicElongation': 'mean',
        'Convexity': 'mean',
        'Solidity': 'mean',
        'MaxFeretDiam': 'mean',
        'Circularity': 'mean',
        'Perimeter_Area_Ratio': 'mean'
    }).unstack().fillna(0)
    class_specific.columns = [
        f"Mean {col[0]} ({col[1]})" for col in class_specific.columns
    ]

    summary_df = (
        class_counts
        .join(total_area)
        .join(overall_means)
        .join(class_area_totals)
        .join(class_specific)
    )

    if all(col in summary_df.columns for col in [ 'Class A Area %', 'Class B Area %', 'Class C Area %', 'Class D Area %']):
        summary_df['Score1'] = (
                1 * summary_df.get('Amount of Class A', 0) +
                2 * summary_df.get('Amount of Class B', 0) +
                3 * summary_df.get('Amount of Class C', 0) +
                4 * summary_df.get('Amount of Class D', 0)
            ) / summary_df['Amount of Particles']

            # Equation 2: Score based on class area percentage
        summary_df['Score2'] = (
                1 * summary_df.get('Class A Area %', 0) +
                2 * summary_df.get('Class B Area %', 0) +
                3 * summary_df.get('Class C Area %', 0) +
                4 * summary_df.get('Class D Area %', 0)
            ) / 100

            # Equation 3: Score based on average of area% and count%
        summary_df['Score3'] = (
                1 * ((summary_df.get('Class A Area %', 0) + 100 * summary_df.get('Amount of Class A', 0) / summary_df['Amount of Particles']) / 2) +
                2 * ((summary_df.get('Class B Area %', 0) + 100 * summary_df.get('Amount of Class B', 0) / summary_df['Amount of Particles']) / 2) +
                3 * ((summary_df.get('Class C Area %', 0) + 100 * summary_df.get('Amount of Class C', 0) / summary_df['Amount of Particles']) / 2) +
                4 * ((summary_df.get('Class D Area %', 0) + 100 * summary_df.get('Amount of Class D', 0) / summary_df['Amount of Particles']) / 2)
            ) / 100
        Mean_Size_Factor_A = (
            df.loc[df['Class'] == 'A']
            .groupby('Image_Source')['Size_Factor']
            .mean()
            .reindex(summary_df.index, fill_value=0)
            )

        Mean_Size_Factor_B = (
            df.loc[df['Class'] == 'B']
            .groupby('Image_Source')['Size_Factor']
            .mean()
            .reindex(summary_df.index, fill_value=0)
            )

        Mean_Size_Factor_C = (
            df.loc[df['Class'] == 'C']
            .groupby('Image_Source')['Size_Factor']
            .mean()
            .reindex(summary_df.index, fill_value=0)
            )

        Mean_Size_Factor_D = (
            df.loc[df['Class'] == 'D']
            .groupby('Image_Source')['Size_Factor']
            .mean()
            .reindex(summary_df.index, fill_value=0)
            )


        
        summary_df['Score4'] = (
                1 * ((summary_df.get('Class A Area %', 0) + 100 * summary_df.get('Amount of Class A', 0) / summary_df['Amount of Particles'] + Mean_Size_Factor_A) / 3) +
                2 * ((summary_df.get('Class B Area %', 0) + 100 * summary_df.get('Amount of Class B', 0) / summary_df['Amount of Particles'] + Mean_Size_Factor_B) / 3) +
                3 * ((summary_df.get('Class C Area %', 0) + 100 * summary_df.get('Amount of Class C', 0) / summary_df['Amount of Particles'] + Mean_Size_Factor_C) / 3) +
                4 * ((summary_df.get('Class D Area %', 0) + 100 * summary_df.get('Amount of Class D', 0) / summary_df['Amount of Particles'] + Mean_Size_Factor_D) / 3)
                ) / 100

        import numpy as np

        # ----------------------------
        # Tunable constants (fixed, not dataset-dependent)
        # ----------------------------
        N0    = 120   # pivot where adjustment is ~0 (your "neutral" particle count)
        lam   = 0.60  # max absolute adjustment (how many score units you can add/subtract)
        soft  = 25    # only used by Option 1 (smaller -> harsher around N0)
        k     = 0.01  # only used by Option 2 (slope; larger -> harsher)
        cap   = lam   # only used by Option 2 (the hard cap)

        # ----------------------------
        # Base blended evidence (counts > area > size)
        # ----------------------------
        N  = summary_df['Amount of Particles'].replace(0, np.nan)
        w_counts, w_area, w_size = 0.5, 0.45, 0.05

        bA = w_counts * (summary_df.get('Amount of Class A', 0) / N) \
           + w_area   * (summary_df.get('Class A Area %', 0) / 100.0) \
           + w_size   * Mean_Size_Factor_A

        bB = w_counts * (summary_df.get('Amount of Class B', 0) / N) \
           + w_area   * (summary_df.get('Class B Area %', 0) / 100.0) \
           + w_size   * Mean_Size_Factor_B

        bC = w_counts * (summary_df.get('Amount of Class C', 0) / N) \
           + w_area   * (summary_df.get('Class C Area %', 0) / 100.0) \
           + w_size   * Mean_Size_Factor_C

        bD = w_counts * (summary_df.get('Amount of Class D', 0) / N) \
           + w_area   * (summary_df.get('Class D Area %', 0) / 100.0) \
           + w_size   * Mean_Size_Factor_D

        total_b = bA + bB + bC + bD
        bA, bB, bC, bD = bA/total_b, bB/total_b, bC/total_b, bD/total_b
        base_score = 1*bA + 2*bB + 3*bC + 4*bD



        N_anchors   = np.array([0,   5,   100, 200, 500, 800, 1000, 1500, 3000], dtype=float)
        S_anchors   = np.array([4.0, 4.0, 3.0, 2.7, 2.0, 1.3, 1.1,  1.0,  1.0], dtype=float)
        score_from_N = np.interp(summary_df['Amount of Particles'].astype(float).values,
                                 N_anchors, S_anchors)
        score_from_N = np.clip(score_from_N, 1.0, 4.0)

        # ----------------------------
        # 3) Blend: pull base_score toward the N-based target
        #    alpha controls severity of particle-count influence.
        # ----------------------------
        alpha = 0.55  # 0.3 = gentle, 0.55 = strong, 0.75 = very strong
        summary_df['Score5'] = np.clip((1 - alpha) * base_score + alpha * score_from_N, 1.0, 4.0)

            # Original score for description
        summary_df['Official_Score'] = summary_df['Score5']
        summary_df['Score_Description'] = summary_df['Official_Score'].apply(interpret_class_score)

    else:
    	print("⚠️ One or more class area % columns missing in summary_df.")



    summary_df.reset_index().to_csv(out_path, index=False)
    print(f" Summary CSV saved to: {out_path}")

# ──────────────────────────────────────────────────────────────
# Entry point
# ──────────────────────────────────────────────────────────────
if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: MLTerminal.py <csv_list_file> <output_csv_path>", file=sys.stderr)
        sys.exit(1)

    list_file = sys.argv[1]
    output_csv_path = sys.argv[2]

    print("Reading CSV paths from:", list_file)
    print("Output will be saved to:", output_csv_path)

    with open(list_file, 'r') as f:
        csv_paths = [line.strip() for line in f if line.strip()]

    super_df = []

    for path in csv_paths:
        try:
            df = pd.read_csv(path)
            run_on_one_df(df, path, super_df)
        except Exception as e:
            print(f"Failed to process {path}: {e}", file=sys.stderr)

    if super_df:
        final_df = pd.concat(super_df, ignore_index=True)
        create_output_csv(final_df, output_csv_path)

        # Optional: open folder and highlight the saved file
        # import subprocess, os
        # subprocess.run(['explorer', f'/select,"{os.path.normpath(output_csv_path)}"'])


# In[ ]:






# In[ ]:




