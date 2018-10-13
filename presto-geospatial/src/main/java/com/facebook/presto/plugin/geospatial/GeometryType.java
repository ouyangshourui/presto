/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.plugin.geospatial;

import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.block.BlockBuilder;
import com.facebook.presto.spi.type.AbstractVariableWidthType;
import com.facebook.presto.spi.type.TypeSignature;
import io.airlift.slice.Slice;

import static com.facebook.presto.geospatial.serde.GeometrySerde.deserialize;

public class GeometryType
        extends AbstractVariableWidthType
{
    public static final GeometryType GEOMETRY = new GeometryType();
    public static final String GEOMETRY_TYPE_NAME = "Geometry";

    private GeometryType()
    {
        super(new TypeSignature(GEOMETRY_TYPE_NAME), Slice.class);
    }

    protected GeometryType(TypeSignature signature)
    {
        super(signature, Slice.class);
    }

    @Override
    public void appendTo(Block block, int position, BlockBuilder blockBuilder)
    {
        if (block.isNull(position)) {
            blockBuilder.appendNull();
        }
        else {
            block.writeBytesTo(position, 0, block.getSliceLength(position), blockBuilder);
            blockBuilder.closeEntry();
        }
    }

    @Override
    public Slice getSlice(Block block, int position)
    {
        return block.getSlice(position, 0, block.getSliceLength(position));
    }

    @Override
    public void writeSlice(BlockBuilder blockBuilder, Slice value)
    {
        if (value == null) {
            blockBuilder.appendNull();
            return;
        }
        writeSlice(blockBuilder, value, 0, value.length());
    }

    @Override
    public void writeSlice(BlockBuilder blockBuilder, Slice value, int offset, int length)
    {
        if (value == null) {
            blockBuilder.appendNull();
            return;
        }
        blockBuilder.writeBytes(value, offset, length).closeEntry();
    }

    @Override
    public Object getObjectValue(ConnectorSession session, Block block, int position)
    {
        if (block.isNull(position)) {
            return null;
        }
        Slice slice = block.getSlice(position, 0, block.getSliceLength(position));
        return deserialize(slice).asText();
    }
}
