import MiniCssExtractPlugin from "mini-css-extract-plugin";
import common from "@kie-tools-core/webpack-base";
import { merge } from "webpack-merge";
import * as path from "path";
// import { env } from "./env";

// const buildEnv: any = env; // build-env is not typed

export default async (env: any, argv: any) => {
  return [
    merge(common(env), {
      entry: ["./src/api/index.ts", "./src/channel/index.ts", "./src/embedded/index.ts", "./src/envelope/index.ts"],
      output: {
        path: path.resolve("dist"),
        filename: "index.js",
        libraryTarget: "umd",
      },
      module: {
        rules: [
          {
            test: /\.scss$/,
            use: [MiniCssExtractPlugin.loader, "css-loader", "sass-loader"],
          },
          {
            test: /\.(tsx|ts)?$/,
            use: "ts-loader",
            exclude: /node_modules/,
          },
        ],
      },
      plugins: [
        new MiniCssExtractPlugin({
          filename: "style.css",
        }),
      ],
      resolve: {
        extensions: [".ts", "tsx", ".js"],
      },
    }),
  ];
};
